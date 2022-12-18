package com.playmonumenta.libraryofsouls.bestiary.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.redissync.MonumentaRedisSyncAPI;
import com.playmonumenta.redissync.event.PlayerSaveEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BestiaryRedisStorage implements BestiaryStorage, Listener {
	private static final String IDENTIFIER = "LOS";
	private static final int MAX_LOAD_PER_TICK = 25;

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
	private final Plugin mPlugin;
	private final Logger mLogger;

	public BestiaryRedisStorage(final Plugin plugin) {
		mPlugin = plugin;
		mLogger = plugin.getLogger();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerJoinEvent(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		mLogger.fine("Started loading redis player data for " + uuid);
		final Long startMainTime = System.currentTimeMillis();

		/* Make sure we don't load data on top of existing data */
		mPlayerOriginalData.remove(uuid);
		mPlayerKills.remove(uuid);

		/* Get player plugin data */
		final JsonObject obj = MonumentaRedisSyncAPI.getPlayerPluginData(uuid, IDENTIFIER);

		if (obj == null) {
			mLogger.info("Bestiary data for player " + player.getName() + " is empty. If they are not new, this is a serious error!");
			mPlayerKills.put(uuid, new HashMap<>());
			mPlayerOriginalData.put(uuid, new JsonObject());
		} else {
			mPlayerOriginalData.put(uuid, obj);
			/*
			 * This is a bit fancy.
			 *
			 * For each soul in the souls database, we compute the minified soul identifier, then see if it was in the JSON data
			 *
			 * Because we don't want to introduce lag when the player logs in, this is spread out over many ticks, but runs on the main thread
			 */
			final SoulsDatabase database = SoulsDatabase.getInstance();
			if (database == null) {
				mLogger.severe("Player joined but SoulsDatabase not initialized!");
				return;
			}

			final Iterator<SoulEntry> iter = database.getSouls().iterator();
			final Map<SoulEntry, Integer> playerKills = new HashMap<>();

			new BukkitRunnable() {
				@Override
				public void run() {
					final Long runnableTime = System.currentTimeMillis();
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
						this.cancel();
						mLogger.fine("Data load complete, total time " + Long.toString(System.currentTimeMillis() - startMainTime) + " milliseconds");
					}
				}
			}.runTaskTimer(mPlugin, 1, 1);
		}
	}

	/* Whenever player data is saved, also save the local data */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerSaveEvent(final PlayerSaveEvent event) {
		/* Have to save the data right now - can't spread it out over multiple ticks (server shutdown, etc.)
		 *
		 * Fortunately this is faster than loading - no strings to parse, no hashmap lookups.
		 *
		 * Still have to wait for Redis to commit though. This can probably be improved later...
		 */
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(uuid);
		final JsonObject originalData = mPlayerOriginalData.get(uuid);
		if (originalData == null) {
			mLogger.severe("Got request to save bestiary data for player " + player.getName() + " before any data has been loaded");
			return;
		}

		mLogger.fine("Started saving redis player data for " + uuid);
		final Long startMainTime = System.currentTimeMillis();

		if (playerKills != null) {
			for (final Map.Entry<SoulEntry, Integer> entry : playerKills.entrySet()) {
				originalData.addProperty(nameToHex(entry.getKey().getLabel()), entry.getValue());
			}
		}

		/* Save the data to Redis */
		event.setPluginData(IDENTIFIER, originalData);

		mLogger.fine("Main thread work took " + Long.toString(System.currentTimeMillis() - startMainTime) + " milliseconds");
	}

	/* When player leaves, remove it from the local storage a short bit later */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerQuitEvent(final PlayerQuitEvent event) {
		Bukkit.getScheduler().runTaskLater(mPlugin, () -> {
			final Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			if (!player.isOnline() && Bukkit.getPlayer(uuid) == null) {
				mPlayerOriginalData.remove(uuid);
				mPlayerKills.remove(uuid);
			}
		}, 100);
	}

	@Override
	public void recordKill(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to record player kill but bestiary data hasn't finished loading yet");
			return;
		}
		mLogger.fine("Recording kill for player " + player.getName() + " mob " + soul.getLabel());

		final Integer kills = playerKills.get(soul);
		if (kills == null) {
			playerKills.put(soul, 1);
		} else {
			playerKills.put(soul, kills + 1);
		}
	}

	@Override
	public int getKillsForMob(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to get kills for mob but bestiary data hasn't finished loading yet");
			return 0;
		}

		final Integer kills = playerKills.get(soul);
		if (kills == null) {
			return 0;
		}
		return kills;
	}

	@Override
	public void setKillsForMob(final Player player, final SoulEntry soul, final int amount) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to set kills for mob but bestiary data hasn't finished loading yet");
			return;
		}

		playerKills.put(soul, amount);
		mPlayerKills.put(player.getUniqueId(), playerKills);
	}

	@Override
	public int addKillsForMob(final Player player, final SoulEntry soul, int amount) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to add kills for mob but bestiary data hasn't finished loading yet");
			return 0;
		}

		amount = playerKills.getOrDefault(soul, 0) + amount;
		playerKills.put(soul, amount);
		mPlayerKills.put(player.getUniqueId(), playerKills);
		return amount;
	}

	@Override
	public Map<SoulEntry, Integer> getAllKilledMobs(final Player player, final Collection<SoulEntry> searchSouls) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to get all killed mobs but bestiary data hasn't finished loading yet");
			return new HashMap<>();
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
