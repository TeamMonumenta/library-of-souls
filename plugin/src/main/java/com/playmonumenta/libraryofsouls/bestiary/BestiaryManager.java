package com.playmonumenta.libraryofsouls.bestiary;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryRedisStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryScoreboardStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryStorage;
import com.playmonumenta.libraryofsouls.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class BestiaryManager implements Listener {
	private static BestiaryManager INSTANCE = null;

	private final BestiaryStorage mStorage;
	private final Logger mLogger;

	public BestiaryManager(Plugin plugin) {
		INSTANCE = this;
		mLogger = plugin.getLogger();

		/*
		 * If MonumentaRedisSync is available, use it for storage
		 * Otherwise fall back to scoreboard storage (which may create a LOT of objectives!)
		 */
		if (Bukkit.getPluginManager().isPluginEnabled("MonumentaRedisSync")) {
			mStorage = new BestiaryRedisStorage(plugin);
			mLogger.info("Using MonumentaRedisSync for bestiary storage");
		} else {
			mStorage = new BestiaryScoreboardStorage();
			mLogger.info("Using scoreboard for bestiary storage");
		}
	}

	public BestiaryManager getInstance() {
		return INSTANCE;
	}

	public static Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) throws Exception {
		if (INSTANCE == null) {
			throw new IllegalStateException("BestiaryManager not initialized!");
		}

		return INSTANCE.mStorage.getAllKilledMobs(player, searchSouls);
	}

	public static int getKillsForMob(Player player, SoulEntry soul) throws Exception {
		if (INSTANCE == null) {
			throw new IllegalStateException("BestiaryManager not initialized!");
		}

		return INSTANCE.mStorage.getKillsForMob(player, soul);
	}

	public static int setKillsForMob(Player player, SoulEntry soul, int amount) throws Exception {
		if (INSTANCE == null) {
			throw new IllegalStateException("BestiaryManager not initialized!");
		}

		return INSTANCE.mStorage.setKillsForMob(player, soul, amount);
	}

	public static int addKillsToMob(Player player, SoulEntry soul, int amount) throws Exception {
		if (INSTANCE == null) {
			throw new IllegalStateException("BestiaryManager not initialized!");
		}

		return INSTANCE.mStorage.addKillsForMob(player, soul, amount);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityDeathEvent(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (!event.isCancelled() && entity instanceof LivingEntity) {
			String name = entity.getCustomName();
			if (name != null && !name.isEmpty()) {
				LivingEntity livingEntity = (LivingEntity)entity;
				Player player = livingEntity.getKiller();
				if (player != null) {
					// Player kills a mob
					SoulsDatabase database = SoulsDatabase.getInstance();
					if (database != null) {
						try {
							String label = Utils.getLabelFromName(name);
							SoulEntry soul = database.getSoul(label);
							if (soul != null) {
								mStorage.recordKill(player, soul);
							}
						} catch (Exception ex) {
							mLogger.warning(ex.getMessage());
						}
					}
				}
			}
		}
	}
}
