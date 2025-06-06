package com.playmonumenta.libraryofsouls.bestiary.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.redissync.ConfigAPI;
import com.playmonumenta.redissync.MonumentaRedisSyncAPI;
import com.playmonumenta.redissync.RedisAPI;
import com.playmonumenta.redissync.event.PlayerSaveEvent;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class BestiaryRedisStorage implements BestiaryStorage, Listener {
	private static final String IDENTIFIER = "LOS";

	/*
	 * This stores the player's current kill data in memory.
	 * These maps can only be interacted with on the main thread!
	 */
	private final Map<UUID, Map<SoulEntry, Integer>> mPlayerKills = new HashMap<>();

	/*
	 * This tracks only kills that have happened since the last save.
	 * This is used for incremental saves to Redis.
	 */
	private final Map<UUID, Map<SoulEntry, Integer>> mPlayerKillsSinceSave = new HashMap<>();

	private final Plugin mPlugin;
	private final Logger mLogger;

	public BestiaryRedisStorage(final Plugin plugin) {
		mPlugin = plugin;
		mLogger = plugin.getLogger();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public static String getRedisBestiaryPath(UUID uuid) {
		return String.format("%s:playerdata:%s:bestiary", ConfigAPI.getServerDomain(), uuid.toString());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerJoinEvent(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		mLogger.fine("Started loading redis player data for " + uuid);
		final Long startMainTime = System.currentTimeMillis();

		/* Make sure we don't load data on top of existing data */
		mPlayerKills.remove(uuid);
		mPlayerKillsSinceSave.remove(uuid);

		/* First check for legacy JSON data and migrate it if present */
		final JsonObject legacyData = MonumentaRedisSyncAPI.getPlayerPluginData(uuid, IDENTIFIER);
		if (legacyData != null && legacyData.size() > 0) {
			mLogger.info("Found legacy bestiary data for player " + player.getName() + ", migrating to new format...");
			migrateLegacyData(player, legacyData);
			return; // Migration will continue the loading process
		}

		/* Load data from Redis hashmap */
		loadBestiaryData(player, startMainTime);
	}

	private void migrateLegacyData(final Player player, final JsonObject legacyData) {
		final UUID uuid = player.getUniqueId();
		final SoulsDatabase database = SoulsDatabase.getInstance();
		if (database == null) {
			mLogger.severe("Player joined but SoulsDatabase not initialized!");
			return;
		}

		final Map<String, String> migrationData = new HashMap<>();
		final Map<SoulEntry, Integer> playerKills = new HashMap<>();

		/* Convert legacy hex-based data to index-based data */
		for (final SoulEntry soul : database.getSouls()) {
			if (soul.getIndex() <= 0) {
				continue; // Skip souls without indices
			}

			final String legacyKey = nameToHex(soul.getLabel());
			final JsonElement element = legacyData.get(legacyKey);
			if (element != null) {
				final int kills = element.getAsInt();
				migrationData.put(String.valueOf(soul.getIndex()), String.valueOf(kills));
				playerKills.put(soul, kills);
			}
		}

		/* Save migrated data to Redis */
		if (!migrationData.isEmpty()) {
			final RedisAsyncCommands<String, String> async = RedisAPI.getInstance().async();
			final String bestiaryPath = getRedisBestiaryPath(uuid);
			async.hmset(bestiaryPath, migrationData);
			mLogger.info("Migrated " + migrationData.size() + " bestiary entries for player " + player.getName());
		}

		/* Set up in-memory data */
		mPlayerKills.put(uuid, playerKills);
		mPlayerKillsSinceSave.put(uuid, new HashMap<>());

		mLogger.fine("Legacy data migration complete");
	}

	private void loadBestiaryData(final Player player, final long startMainTime) {
		final UUID uuid = player.getUniqueId();
		final String bestiaryPath = getRedisBestiaryPath(uuid);

		/* Get all bestiary data from Redis hashmap */
		final RedisFuture<Map<String, String>> future = RedisAPI.getInstance().async().hgetall(bestiaryPath);

		Bukkit.getScheduler().runTaskAsynchronously(mPlugin, () -> {
			try {
				final Map<String, String> redisData = future.get(30, TimeUnit.SECONDS);

				Bukkit.getScheduler().runTask(mPlugin, () -> {
					if (!player.isOnline()) {
						return;
					}

					final SoulsDatabase database = SoulsDatabase.getInstance();
					if (database == null) {
						mLogger.severe("Player joined but SoulsDatabase not initialized!");
						return;
					}

					final Map<SoulEntry, Integer> playerKills = new HashMap<>();

					if (redisData != null && !redisData.isEmpty()) {
						/* Convert Redis data back to SoulEntry -> Integer mapping */
						for (final Map.Entry<String, String> entry : redisData.entrySet()) {
							try {
								final int mobIndex = Integer.parseInt(entry.getKey());
								final int kills = Integer.parseInt(entry.getValue());
								final SoulEntry soul = database.getSoulByIndex(mobIndex);
								if (soul != null) {
									playerKills.put(soul, kills);
								}
							} catch (NumberFormatException e) {
								mLogger.warning("Invalid bestiary data for player " + uuid + ": " + entry.getKey() + " -> " + entry.getValue());
							}
						}
					}

					mPlayerKills.put(uuid, playerKills);
					mPlayerKillsSinceSave.put(uuid, new HashMap<>());

					mLogger.fine("Bestiary data load complete, total time " + (System.currentTimeMillis() - startMainTime) + " milliseconds");
				});
			} catch (Exception e) {
				mLogger.severe("Failed to load bestiary data for player " + player.getName() + ": " + e.getMessage());
				Bukkit.getScheduler().runTask(mPlugin, () -> {
					if (player.isOnline()) {
						mPlayerKills.put(uuid, new HashMap<>());
						mPlayerKillsSinceSave.put(uuid, new HashMap<>());
					}
				});
			}
		});
	}

	/* Whenever player data is saved, also save the incremental bestiary data */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerSaveEvent(final PlayerSaveEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(uuid);

		if (killsSinceSave == null || killsSinceSave.isEmpty()) {
			/* No bestiary changes to save */
			return;
		}

		mLogger.fine("Started saving redis bestiary data for " + uuid);
		final long startMainTime = System.currentTimeMillis();

		/* Use HINCRBY to increment kill counts in Redis */
		final RedisAsyncCommands<String, String> async = RedisAPI.getInstance().async();
		final String bestiaryPath = getRedisBestiaryPath(uuid);

		async.multi();
		for (final Map.Entry<SoulEntry, Integer> entry : killsSinceSave.entrySet()) {
			final SoulEntry soul = entry.getKey();
			if (soul.getIndex() > 0) {
				async.hincrby(bestiaryPath, String.valueOf(soul.getIndex()), entry.getValue());
			}
		}
		async.exec();

		/* Clear the since-save tracking */
		killsSinceSave.clear();

		mLogger.fine("Bestiary save took " + (System.currentTimeMillis() - startMainTime) + " milliseconds");
	}

	/* When player leaves, remove it from the local storage a short bit later */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerQuitEvent(final PlayerQuitEvent event) {
		Bukkit.getScheduler().runTaskLater(mPlugin, () -> {
			final Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			if (!player.isOnline() && Bukkit.getPlayer(uuid) == null) {
				mPlayerKills.remove(uuid);
				mPlayerKillsSinceSave.remove(uuid);
			}
		}, 100);
	}

	@Override
	public void recordKill(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(player.getUniqueId());

		if (playerKills == null || killsSinceSave == null) {
			mLogger.severe("Attempted to record player kill but bestiary data hasn't finished loading yet");
			return;
		}

		if (soul.getIndex() <= 0) {
			mLogger.warning("Attempted to record kill for soul without index: " + soul.getLabel());
			return;
		}

		mLogger.fine("Recording kill for player " + player.getName() + " mob " + soul.getLabel());

		playerKills.merge(soul, 1, Integer::sum);
		killsSinceSave.merge(soul, 1, Integer::sum);
	}

	@Override
	public int getKillsForMob(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			mLogger.severe("Attempted to get kills for mob but bestiary data hasn't finished loading yet");
			return 0;
		}

		return playerKills.getOrDefault(soul, 0);
	}

	@Override
	public void setKillsForMob(final Player player, final SoulEntry soul, final int amount) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(player.getUniqueId());

		if (playerKills == null || killsSinceSave == null) {
			mLogger.severe("Attempted to set kills for mob but bestiary data hasn't finished loading yet");
			return;
		}

		if (soul.getIndex() <= 0) {
			mLogger.warning("Attempted to set kills for soul without index: " + soul.getLabel());
			return;
		}

		/* Update in-memory data */
		playerKills.put(soul, amount);

		/* Clear any pending incremental save for this mob */
		killsSinceSave.remove(soul);

		/* Immediately set the value in Redis */
		final String bestiaryPath = getRedisBestiaryPath(player.getUniqueId());
		RedisAPI.getInstance().async().hset(bestiaryPath, String.valueOf(soul.getIndex()), String.valueOf(amount));
	}

	@Override
	public int addKillsForMob(final Player player, final SoulEntry soul, int amount) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(player.getUniqueId());

		if (playerKills == null || killsSinceSave == null) {
			mLogger.severe("Attempted to add kills for mob but bestiary data hasn't finished loading yet");
			return 0;
		}

		if (soul.getIndex() <= 0) {
			mLogger.warning("Attempted to add kills for soul without index: " + soul.getLabel());
			return playerKills.getOrDefault(soul, 0);
		}

		final int newAmount = playerKills.getOrDefault(soul, 0) + amount;
		playerKills.put(soul, newAmount);
		killsSinceSave.merge(soul, amount, Integer::sum);

		return newAmount;
	}

	@Override
	public Map<SoulEntry, Integer> getAllKilledMobs(final Player player, final Collection<SoulEntry> searchSouls) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			mLogger.severe("Attempted to get all killed mobs but bestiary data hasn't finished loading yet");
			return new HashMap<>();
		}

		final Map<SoulEntry, Integer> map = new HashMap<>();

		for (final SoulEntry soul : searchSouls) {
			map.put(soul, playerKills.getOrDefault(soul, 0));
		}

		return map;
	}

	private static String nameToHex(final String name) {
		return Integer.toHexString(name.hashCode());
	}
}
