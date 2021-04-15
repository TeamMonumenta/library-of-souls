package com.playmonumenta.libraryofsouls.bestiary;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class BestiaryManager implements Listener {
	private static BestiaryManager INSTANCE = null;

	private final BestiaryStorage mStorage;
	private final Logger mLogger;
	private final Map<Entity, Set<Player>> mDamageTracker = new HashMap<>();

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

		// Schedule a slow-ticking task to clean stale junk out of the map periodically
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			Iterator<Map.Entry<Entity, Set<Player>>> iter = mDamageTracker.entrySet().iterator();
			while (iter.hasNext()) {
				if (!iter.next().getKey().isValid()) {
					iter.remove();
				}
			}
			if (mDamageTracker.size() > 50) {
				mLogger.warning("There are " + mDamageTracker.size() + " entries in the damage tracker which is abnormally high. Maybe a bug?");
			}
		}, 1150, 1150);
	}

	public BestiaryManager getInstance() {
		return INSTANCE;
	}

	public static Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) {
		if (INSTANCE == null) {
			LibraryOfSouls.getInstance().getLogger().severe("BestiaryManager not initialized!");
			return Collections.emptyMap();
		}

		return INSTANCE.mStorage.getAllKilledMobs(player, searchSouls);
	}

	public static int getKillsForMob(Player player, SoulEntry soul) {
		if (INSTANCE == null) {
			LibraryOfSouls.getInstance().getLogger().severe("BestiaryManager not initialized!");
			return 0;
		}

		return INSTANCE.mStorage.getKillsForMob(player, soul);
	}

	public static void setKillsForMob(Player player, SoulEntry soul, int amount) {
		if (INSTANCE == null) {
			LibraryOfSouls.getInstance().getLogger().severe("BestiaryManager not initialized!");
			return;
		}

		INSTANCE.mStorage.setKillsForMob(player, soul, amount);
	}

	public static int addKillsToMob(Player player, SoulEntry soul, int amount) {
		if (INSTANCE == null) {
			LibraryOfSouls.getInstance().getLogger().severe("BestiaryManager not initialized!");
			return 0;
		}

		return INSTANCE.mStorage.addKillsForMob(player, soul, amount);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		Entity damager = event.getDamager();
		if (!event.isCancelled() && entity instanceof LivingEntity &&
		    !(entity instanceof Player) && damager instanceof Player) {
			// Non-player living entity was damaged by player

			String name = entity.getCustomName();
			if (name != null && !name.isEmpty()) {
				// Damaged entity had a non-empty name

				Set<String> tags = entity.getScoreboardTags();
				if (tags != null && !tags.isEmpty() && tags.contains("Boss")) {
					// Damaged entity has the Boss tag

					// Keep track of all players that damage this mob
					Set<Player> damagers = mDamageTracker.get(entity);
					if (damagers == null) {
						// Hasn't been damaged yet - create new set of damaging players
						damagers = new HashSet<>();
						damagers.add((Player)damager);
						mDamageTracker.put(entity, damagers);
					} else {
						// Has been damaged already - add player to set
						damagers.add((Player)damager);
					}
				}
			}
		}
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
								// A soul entry exists for this mob

								// Check if this was a boss with multiple tracked damagers
								Set<Player> damagers = mDamageTracker.remove(entity);
								if (damagers != null) {
									// A boss, record kill for everyone who damaged the mob (including the killer)
									damagers.add(player);
									for (Player damager : damagers) {
										try {
											mStorage.recordKill(damager, soul);
										} catch (Exception ex) {
											mLogger.warning(ex.getMessage());
										}
									}
								} else {
									// Not a boss, just record kill for the killer
									try {
										mStorage.recordKill(player, soul);
									} catch (Exception ex) {
										mLogger.warning(ex.getMessage());
									}
								}
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
