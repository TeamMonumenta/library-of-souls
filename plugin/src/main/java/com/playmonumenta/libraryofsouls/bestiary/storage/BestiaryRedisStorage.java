package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.redissync.MonumentaRedisSyncAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BestiaryRedisStorage implements BestiaryStorage {
	private static final String IDENTIFIER = "LOS";
	private static final int MAX_LOAD_PER_TICK = 25;
	private static final int SAVE_PERIOD = 20 * 60 * 6; // 6 minutes

	/*
	 * This stores the original JSON string containing the player's data. Data is merged into
	 * this original data before saving.
	 *
	 * This way, if a shard happens to load a broken version of the souls database, players
	 * who log in don't lose their data
	 *
	 * These maps can only be interacted with on the main thread!
	 */
	private final Map<UUID, JsonObject> mPlayerOriginalData = new HashMap<>();
	private final Map<UUID, Map<SoulEntry, Integer>> mPlayerKills = new HashMap<>();
	private final Gson mGson = new Gson();
	private final Plugin mPlugin;
	private final Logger mLogger;


	public BestiaryRedisStorage(final Plugin plugin) {
		mPlugin = plugin;
		mLogger = plugin.getLogger();
	}

	@Override
	public void load(final Player player, final SoulsDatabase database) {
		final UUID uuid = player.getUniqueId();
		mLogger.fine("Started loading redis player data for " + uuid);
		Long startMainTime = System.currentTimeMillis();

		/* Make sure we don't load data on top of existing data */
		mPlayerOriginalData.remove(uuid);
		mPlayerKills.remove(uuid);

		/* Start loading the data async, store the resulting future for later evaluation */
		final CompletableFuture<String> data = MonumentaRedisSyncAPI.loadPlayerPluginData(uuid, IDENTIFIER);

		Long startAsyncTime = System.currentTimeMillis();
		Bukkit.getScheduler().runTaskAsynchronously(mPlugin, () -> {
			String jsonData;
			try {
				mLogger.fine("Getting to async task took " + Long.toString(System.currentTimeMillis() - startAsyncTime) + " milliseconds");
				jsonData = data.get();
				mLogger.fine("Getting data took " + Long.toString(System.currentTimeMillis() - startAsyncTime) + " milliseconds");
				if (jsonData == null || jsonData.isEmpty()) {
					mLogger.warning("Bestiary data for player " + player.getName() + " is empty. If they are not new, this is a serious error!");

					Bukkit.getScheduler().runTask(mPlugin, () -> {
						mPlayerKills.put(uuid, new HashMap<>());
						mPlayerOriginalData.put(uuid, new JsonObject());
					});
				} else {
					final JsonObject obj = mGson.fromJson(jsonData, JsonObject.class);

					/*
					 * This is a bit fancy.
					 *
					 * For each soul in the souls database, we compute the minified soul identifier, then see if it was in the JSON data
					 *
					 * Because we don't want to introduce lag when the player logs in, this is spread out over many ticks, but runs on the main thread
					 */
					final Iterator<SoulEntry> iter = database.getSouls().iterator();
					final Map<SoulEntry, Integer> playerKills = new HashMap<>();

					new BukkitRunnable() {
						@Override
						public void run() {
							Long runnableTime = System.currentTimeMillis();
							int stepCounter = 0;
							while (iter.hasNext() && player.isOnline()) {
								final SoulEntry soul = iter.next();

								/*
								 * Check if the player's JSON data contains the hashed/hex mob label.
								 * If so, store how many kills in the active player data
								 */
								final String key = nameToHex(soul.getLabel());
								final JsonElement element = obj.get(key);
								if (element != null) {
									playerKills.put(soul, element.getAsInt());
								}

								stepCounter++;
								if (stepCounter >= MAX_LOAD_PER_TICK) {
									/* Will continue where this left off on the next tick */
									break;
								}
							}
							mLogger.fine("Main thread data loading loop took " + Long.toString(System.currentTimeMillis() - runnableTime) + " milliseconds");

							if (!player.isOnline()) {
								/* Player logged out before their data finished loading - abort */
								this.cancel();
							} else if (!iter.hasNext()) {
								/* All done loading - make the player kills map accessible */
								mPlayerKills.put(uuid, playerKills);
								mPlayerOriginalData.put(uuid, obj);
								this.cancel();
								mLogger.fine("Data load complete, total time " + Long.toString(System.currentTimeMillis() - startMainTime) + " milliseconds");

								/* Start an autosave task for this player */
								new BukkitRunnable() {
									@Override
									public void run() {
										if (player.isOnline()) {
											save(player, false);
										} else {
											this.cancel();
										}
									}
								}.runTaskTimer(mPlugin, SAVE_PERIOD, SAVE_PERIOD);
							}
						}
					}.runTaskTimer(mPlugin, 1, 1);
				}
			} catch (InterruptedException | ExecutionException e) {
				mLogger.severe("Failed to retrieve bestiary data for player " + player.getName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		});
		mLogger.fine("Main thread work took " + Long.toString(System.currentTimeMillis() - startMainTime) + " milliseconds");
	}

	private void save(final Player player, final boolean waitForCommit) {
		/* Have to save the data right now - can't spread it out over multiple ticks (server shutdown, etc.)
		 *
		 * Fortunately this is faster than loading - no strings to parse, no hashmap lookups.
		 *
		 * Still have to wait for Redis to commit though. This can probably be improved later...
		 */
		final UUID uuid = player.getUniqueId();
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(uuid);
		final JsonObject originalData = mPlayerOriginalData.get(uuid);
		if (playerKills == null || originalData == null) {
			/* Nothing to do */
			return;
		}

		mLogger.fine("Started saving redis player data for " + uuid);
		Long startMainTime = System.currentTimeMillis();

		for (final Map.Entry<SoulEntry, Integer> entry : playerKills.entrySet()) {
			originalData.addProperty(nameToHex(entry.getKey().getLabel()), entry.getValue());
		}

		/* Save the data to Redis */
		final CompletableFuture<Boolean> commit = MonumentaRedisSyncAPI.savePlayerPluginData(uuid, IDENTIFIER, mGson.toJson(originalData));

		/* Only wait & drive this to completion when waitForCommit is true (log out, server stop, etc.) */
		if (waitForCommit) {
			try {
				commit.get();
			} catch (InterruptedException | ExecutionException e) {
				mLogger.severe("Failed to commit bestiary data for player " + player.getName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		mLogger.fine("Main thread work took " + Long.toString(System.currentTimeMillis() - startMainTime) + " milliseconds (wait for commit=" + Boolean.toString(waitForCommit) + ")");
	}

	@Override
	public void save(final Player player) {
		save(player, true);
	}

	@Override
	public void recordKill(final Player player, final SoulEntry soul) throws Exception {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			throw new Exception("Bestiary data hasn't finished loading yet!");
		}
		mLogger.fine("Recording kill for player " + player.getName() + " mob " + soul.getLabel());

		final Integer kills = playerKills.get(soul);
		if (kills == null) {
			playerKills.put(soul, 1);
		} else  {
			playerKills.put(soul, kills + 1);
		}
	}

	@Override
	public int getKillsForMob(final Player player, final SoulEntry soul) throws Exception {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			throw new Exception("Bestiary data hasn't finished loading yet!");
		}

		Integer kills = playerKills.get(soul);
		if (kills == null) {
			return 0;
		}
		return kills;
	}

	@Override
	public int setKillsForMob(final Player player, final SoulEntry soul, final int amount) throws Exception {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			throw new Exception("Bestiary data hasn't finished loading yet!");
		}

		playerKills.put(soul, amount);
		mPlayerKills.put(player.getUniqueId(), playerKills);
		return amount;
	}

	@Override
	public int addKillsForMob(final Player player, final SoulEntry soul, int amount) throws Exception {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			throw new Exception("Bestiary data hasn't finished loading yet!");
		}

		amount = playerKills.get(soul) + amount;
		playerKills.put(soul, amount);
		mPlayerKills.put(player.getUniqueId(), playerKills);
		return amount;
	}

	@Override
	public Map<SoulEntry, Integer> getAllKilledMobs(final Player player, final Collection<SoulEntry> searchSouls) throws Exception {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			throw new Exception("Bestiary data hasn't finished loading yet!");
		}

		final Map<SoulEntry, Integer> map = new HashMap<>();

		for (final SoulEntry soul : searchSouls) {
			if (playerKills == null) {
				map.put(soul, 0);
			} else {
				map.put(soul, playerKills.getOrDefault(soul, 0));
			}
		}

		return map;
	}

	private static String nameToHex(final String name) {
		return Integer.toHexString(name.hashCode());
	}
}
