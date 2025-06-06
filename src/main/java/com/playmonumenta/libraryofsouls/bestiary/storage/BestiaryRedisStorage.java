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
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * This class provides a Redis-based storage solution for the Library of Souls bestiary.
 * The design is roughly:
 * - Incremental player kills are tracked and saved to redis by HINCRBY. The
 *   kills themselves aren't set directly, so if they fail to load, they're never
 *   overwritten.
 * - Total kill counts (for actually looking at bestiary in-game) are loaded
 *   over several ticks after the player joins, they are not all available
 *   immediately.
 */
public class BestiaryRedisStorage implements BestiaryStorage, Listener {
	private static final String LEGACY_PLUGINDATA_IDENTIFIER = "LOS";
	private static final int MAX_LOAD_PER_TICK = 100;

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
		final JsonObject legacyData = MonumentaRedisSyncAPI.getPlayerPluginData(uuid, LEGACY_PLUGINDATA_IDENTIFIER);
		if (legacyData != null && legacyData.size() > 0) {
			mLogger.info("Found legacy bestiary data for player " + player.getName() + ", migrating to new format...");
			// For player login upgrades, wait for them to commit successfully, even if that causes lag
			migrateLegacyPluginData(player, legacyData, true /* Wait for commit */);
			return; // Migration will continue the loading process
		}

		/* Load data from Redis hashmap */
		loadBestiaryData(player, startMainTime);
	}

	/* If waitForRedisCommit is true, wait for the Redis commit to complete before continuing, potentially holding up the shard */
	private void migrateLegacyPluginData(final Player player, final JsonObject legacyData, boolean waitForRedisCommit) {
		final UUID uuid = player.getUniqueId();
		final SoulsDatabase database = SoulsDatabase.getInstance();

		final Map<String, String> migrationData = new HashMap<>();
		final Map<SoulEntry, Integer> playerKills = new HashMap<>();

		mLogger.fine("Legacy data migration for player " + player.getName() + " started");

		/* Convert legacy hex-based data to index-based data */
		for (final SoulEntry soul : database.getSouls()) {
			final String legacyKey = nameToLegacyHex(soul.getLabel());
			final JsonElement element = legacyData.get(legacyKey);
			if (element != null) {
				final int kills = element.getAsInt();
				migrationData.put(String.valueOf(soul.getIndex()), String.valueOf(kills));
				playerKills.put(soul, kills);
			}
		}

		/* Save migrated data to Redis */
		if (!migrationData.isEmpty()) {
			final RedisAsyncCommands<String, String> redis = RedisAPI.getInstance().async();
			RedisFuture<String> future = redis.hmset(getRedisBestiaryPath(uuid), migrationData);
			future.whenComplete((result, error) -> {
				if (error != null) {
					player.sendMessage(Component.text("Failed to migrate your bestiary data to redis. This is a critical problem - your bestiary has been wiped. Please log off and ping a moderator for assistance.", NamedTextColor.RED));
					mLogger.severe("Failed to commit bestiary data for player " + player.getName() + ": " + error.getMessage());
					return;
				}
				mLogger.info("Migrated " + migrationData.size() + " bestiary entries for player " + player.getName());
			});
			if (waitForRedisCommit) {
				// Caller requested to wait until the commit is complete
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					// If this happens, the future should also complete exceptionally and print a similar error
					mLogger.severe("Timed out waiting to commit bestiary data for player " + player.getName() + ": " + e.getMessage());
				}
			}
		}

		/* Set up in-memory data */
		mPlayerKills.put(uuid, playerKills);
		mPlayerKillsSinceSave.put(uuid, new HashMap<>());

		mLogger.fine("Legacy data migration for player " + player.getName() + " complete");
	}

	private void loadBestiaryData(final Player player, final long startMainTime) {
		final UUID uuid = player.getUniqueId();

		/* Start tracking new kills immediately */
		mPlayerKillsSinceSave.put(uuid, new HashMap<>());

		/* Get all bestiary data from Redis hashmap, then parse it over the next several ticks */
		RedisAPI.getInstance().async().hgetall(getRedisBestiaryPath(uuid)).whenComplete((redisData, error) -> {
			if (error != null) {
				Bukkit.getScheduler().runTask(mPlugin, () -> {
					player.sendMessage(Component.text("Your bestiary data failed to load. You can probably fix this by relogging, if this persists please report this bug. No data has been lost.", NamedTextColor.RED));
					mLogger.warning("Failed to load bestiary data (though no data was lost) for player " + player.getName() + ": " + error.getMessage());
				});

				return;
			}

			final Iterator<Map.Entry<String, String>> iter = redisData.entrySet().iterator();
			final Map<SoulEntry, Integer> playerKills = new HashMap<>();
			final SoulsDatabase database = SoulsDatabase.getInstance();

			/* Load the data over the next several ticks, looking up the souls by index */
			new BukkitRunnable() {
				@Override
				public void run() {
					final long runnableTime = System.currentTimeMillis();

					int stepCounter = 0;
					while (iter.hasNext() && player.isOnline()) {
						Map.Entry<String, String> entry = iter.next();

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

						stepCounter++;
						if (stepCounter >= MAX_LOAD_PER_TICK) {
							/* Will continue where this left off on the next tick */
							break;
						}
					}

					mLogger.fine("Bestiary loading iteration for player " + player.getName() + " took " + (System.currentTimeMillis() - runnableTime) + " milliseconds on main thread");

					if (!player.isOnline()) {
						/* Player logged out before their data finished loading - abort */
						this.cancel();
					} else if (!iter.hasNext()) {
						/* All done loading - make the player kills map accessible */
						mLogger.fine("Bestiary loading for player " + player.getName() + " took " + (System.currentTimeMillis() - startMainTime) + " milliseconds overall");
						mPlayerKills.put(uuid, playerKills);
						this.cancel();
					}
				}
			}.runTaskTimer(mPlugin, 1, 1);
		});
	}

	/* Whenever player data is saved, also save the incremental bestiary data */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerSaveEvent(final PlayerSaveEvent event) {
		savePlayerBestiaryData(event.getPlayer());
	}

	public void savePlayerBestiaryData(final Player player) {
		final UUID uuid = player.getUniqueId();
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(uuid);

		if (killsSinceSave == null || killsSinceSave.isEmpty()) {
			/* No bestiary changes to save */
			return;
		}

		/* Use HINCRBY to increment kill counts in Redis */
		final RedisAsyncCommands<String, String> async = RedisAPI.getInstance().async();
		final String bestiaryPath = getRedisBestiaryPath(uuid);

		async.multi();
		for (final Map.Entry<SoulEntry, Integer> entry : killsSinceSave.entrySet()) {
			final SoulEntry soul = entry.getKey();
			async.hincrby(bestiaryPath, String.valueOf(soul.getIndex()), entry.getValue());
		}
		RedisFuture<TransactionResult> future = async.exec();

		future.whenComplete((result, error) -> {
			if (error != null) {
				mLogger.severe("Failed to save incremental bestiary kills for player " + player.getName() + ": " + error.getMessage());
			} else {
				mLogger.fine("Saved " + killsSinceSave.size() + " incremental bestiary mobs with recent kills for player " + player.getName());
			}
		});

		if (Bukkit.getServer().isStopping()) {
			// If the server is stopping, wait for data to commit before proceeding
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				mLogger.warning("Timeout while waiting for bestiary data to commit during server shutdown");
			}
		}

		/* Clear the since-save tracking */
		killsSinceSave.clear();
	}

	/* When player leaves, remove it from the local storage a short bit later */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void playerQuitEvent(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();

		savePlayerBestiaryData(player);
		mPlayerKills.remove(uuid);
		mPlayerKillsSinceSave.remove(uuid);
	}

	@Override
	public void recordKill(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(player.getUniqueId());

		mLogger.fine("Recording kill for player " + player.getName() + " mob " + soul.getLabel());

		if (playerKills != null) {
			playerKills.merge(soul, 1, Integer::sum);
		} else {
			// This isn't really a problem, as the data isn't actually written back anywhere, it may just appear to be less than it really is for this session
			mLogger.info("Attempted to record bestiary kill for player '" + player.getName() + "' but their overall kills haven't finished loading yet.");
		}

		if (killsSinceSave != null) {
			killsSinceSave.merge(soul, 1, Integer::sum);
		} else {
			// This shouldn't happen, as this map should be available immediately after login
			mLogger.severe("Attempted to record bestiary kill for player '" + player.getName() + "' but they don't have a killsSinceSave map");
		}
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

		/* Set the value directly in Redis */
		final String bestiaryPath = getRedisBestiaryPath(player.getUniqueId());
		RedisAPI.getInstance().async().hset(bestiaryPath, String.valueOf(soul.getIndex()), String.valueOf(amount))
		.whenComplete((result, error) -> {
			if (error != null) {
				mLogger.severe("Failed to set kills for mob in Redis for player " + player.getName() + ": " + error.getMessage());
			} else {
				mLogger.fine("Set kills for mob in Redis for player " + player.getName() + " to " + amount);
			}
		});

		if (playerKills == null) {
			mLogger.warning("Attempted to set kills for mob but bestiary data hasn't finished loading yet. The displayed value will be wrong until the player logs in again.");
		} else {
			/* Update in-memory data */
			playerKills.put(soul, amount);
		}

		/* Clear any pending incremental save for this mob */
		if (killsSinceSave != null) {
			killsSinceSave.remove(soul);
		} else {
			// This shouldn't happen, as this map should be available immediately after login
			mLogger.severe("While setting bestiary kills for player '" + player.getName() + "' they don't have a killsSinceSave map");
		}
	}

	/**
	 * Adds kills for a mob to the player's bestiary data.
	 *
	 * @param player the player whose bestiary data to update
	 * @param soul the soul entry for the mob
	 * @param amount the number of kills to ad
	 * @return
	 * > 0 : the new total number of kills for the mob. The returned value
	 *   0 : if the player's bestiary data is still being loaded, which may
	 *       take several seconds after login. Even if this happens the added kill
	 *       count will be successfully recorded in redis
	 *  -1 : the specified soul doesn't have an index, so there's no way to record this kill
	 */
	@Override
	public int addKillsForMob(final Player player, final SoulEntry soul, int amount) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		final Map<SoulEntry, Integer> killsSinceSave = mPlayerKillsSinceSave.get(player.getUniqueId());

		if (killsSinceSave == null) {
			// This shouldn't happen, as this map should be available immediately after login
			mLogger.severe("Attempted to record bestiary kill for player '" + player.getName() + "' but they don't have a killsSinceSave map");
		} else {
			killsSinceSave.merge(soul, amount, Integer::sum);
		}

		if (playerKills == null) {
			mLogger.warning("Attempted to add kills for mob but overall bestiary data hasn't finished loading yet. The displayed value will be wrong until the player logs in again.");
			return 0;
		} else {
			return playerKills.merge(soul, amount, Integer::sum);
		}
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

	private static String nameToLegacyHex(final String name) {
		return Integer.toHexString(name.hashCode());
	}
}
