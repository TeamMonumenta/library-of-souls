package com.playmonumenta.libraryofsouls.bestiary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryRedisStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryScoreboardStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryStorage;
import com.playmonumenta.libraryofsouls.utils.Utils;

public class BestiaryManager implements Listener {
	private static BestiaryManager INSTANCE = null;

	private final BestiaryStorage mStorage;
	private final Logger mLogger;

	public BestiaryManager(Logger logger) {
		INSTANCE = this;
		mLogger = logger;

		/*
		 * If MonumentaRedisSync is available, use it for storage
		 * Otherwise fall back to scoreboard storage (which may create a LOT of objectives!)
		 */
		if (Bukkit.getPluginManager().isPluginEnabled("MonumentaRedisSync")) {
			mStorage = new BestiaryRedisStorage();
			mLogger.info("Using MonumentaRedisSync for bestiary storage");
		} else {
			mStorage = new BestiaryScoreboardStorage();
			mLogger.info("Using scoreboard for bestiary storage");
		}
	}

	public BestiaryManager getInstance() {
		return INSTANCE;
	}

	public static Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) {
		if (INSTANCE == null) {
			return new HashMap<>();
		}

		return INSTANCE.mStorage.getAllKilledMobs(player, searchSouls);
	}

	public static int getKillsForMob(Player player, String name) {
		if (INSTANCE == null) {
			return -1;
		}

		return INSTANCE.mStorage.getKillsForMob(player, name);
	}

	public static int bindKillsToEntry(Player player, String name, String objName) {
		if (INSTANCE == null) {
			return -1;
		}

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = scoreboard.getObjective(objName);
		if (SoulsDatabase.getInstance().getSoul(name) == null) {
			return -1;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		Score score = objective.getScore(player.getDisplayName());
		score.setScore(INSTANCE.mStorage.getKillsForMob(player, name));
		return score.getScore();
	}

	public static int bindKillsToEntry(Player player, String name, String objName, String entryName) {
		if (INSTANCE == null) {
			return -1;
		}

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = scoreboard.getObjective(objName);
		if (SoulsDatabase.getInstance().getSoul(name) == null) {
			return -1;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		if (objective == null) {
			objective = scoreboard.registerNewObjective(objName, "dummy", soul.getName() + ChatColor.WHITE + " kills");
		}

		Score score = objective.getScore(entryName);
		score.setScore(INSTANCE.mStorage.getKillsForMob(player, name));
		Iterator<Advancement> advance = Bukkit.advancementIterator();
		while (advance.hasNext()) {
			Bukkit.broadcastMessage(advance.next().getKey().getKey());
		}
		return score.getScore();
	}

	public static boolean setKillsForMob(Player player, String name, int amount) {
		if (INSTANCE == null) {
			return false;
		}

		return INSTANCE.mStorage.setKillsForMob(player, name, amount);
	}

	public static boolean addKillsToMob(Player player, String name, int amount) {
		if (INSTANCE == null) {
			return false;
		}

		return INSTANCE.mStorage.addKillsToMob(player, name, amount);
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
