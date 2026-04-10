package com.playmonumenta.libraryofsouls.bestiary.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.redissync.MonumentaRedisSyncAPI;
import com.playmonumenta.redissync.event.PlayerSaveEvent;
import java.util.Collection;
import java.util.HashMap;
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

public class BestiaryRedisStorage implements BestiaryStorage, Listener {
	private static final String IDENTIFIER = "LOS";

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
	private final Map<String, SoulEntry> mSoulLookupCache = new HashMap<>();
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
			final SoulsDatabase database = SoulsDatabase.getInstance();
			if (database == null) {
				mLogger.severe("Player joined but SoulsDatabase not initialized!");
				return;
			}

			if (mSoulLookupCache.isEmpty()) {
				for (SoulEntry soul : database.getSouls()) {
					mSoulLookupCache.put(nameToHex(soul.getLabel()), soul);
				}
			}

			final Map<SoulEntry, Integer> playerKills = new HashMap<>();

			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				final SoulEntry soul = mSoulLookupCache.get(entry.getKey());
				if (soul != null) {
					playerKills.put(soul, entry.getValue().getAsInt());
				}
			}

			mPlayerKills.put(uuid, playerKills);
			mLogger.fine("Data load complete, total time " + (System.currentTimeMillis() - startMainTime) + " milliseconds");
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
		final long startMainTime = System.currentTimeMillis();

		if (playerKills != null) {
			for (final Map.Entry<SoulEntry, Integer> entry : playerKills.entrySet()) {
				originalData.addProperty(nameToHex(entry.getKey().getLabel()), entry.getValue());
			}
		}

		/* Save the data to Redis */
		event.setPluginData(IDENTIFIER, originalData);

		mLogger.fine("Main thread work took " + (System.currentTimeMillis() - startMainTime) + " milliseconds");
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

		playerKills.merge(soul, 1, Integer::sum);
	}

	@Override
	public int getKillsForMob(final Player player, final SoulEntry soul) {
		final Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			/* TODO: Instead of throwing an exception, should force complete this on the main thread */
			mLogger.severe("Attempted to get kills for mob but bestiary data hasn't finished loading yet");
			return 0;
		}

		return playerKills.getOrDefault(soul, 0);
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
			map.put(soul, playerKills.getOrDefault(soul, 0));
		}

		return map;
	}

	private static String nameToHex(final String name) {
		return Integer.toHexString(name.hashCode());
	}
}
