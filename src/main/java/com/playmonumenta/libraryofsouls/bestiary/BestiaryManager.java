package com.playmonumenta.libraryofsouls.bestiary;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryRedisStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryScoreboardStorage;
import com.playmonumenta.libraryofsouls.bestiary.storage.BestiaryStorage;
import com.playmonumenta.libraryofsouls.utils.Utils;
import java.util.*;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.Nullable;

public class BestiaryManager implements Listener {
	private static @Nullable BestiaryManager INSTANCE = null;

	private final BestiaryStorage mStorage;
	private final Logger mLogger;

	private static final int MAX_BOSS_TRACK_ENTRIES = 30;

	/*
	 * A least-recently-used (LRU) map to keep track of which players damage which bosses
	 * Entity UUID key, Player UUID Set
	 * The size here is automatically managed by removing the oldest entries as the map fills up
	 */
	private final Map<UUID, Set<UUID>> mDamageTracker = new LinkedHashMap<>(MAX_BOSS_TRACK_ENTRIES + 1,
		.75F, true) {
		// This method is called just after a new entry has been added
		@Override
		public boolean removeEldestEntry(Map.Entry<UUID, Set<UUID>> eldest) {
			return size() > MAX_BOSS_TRACK_ENTRIES;
		}
	};

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
		if (INSTANCE == null) {
			throw new RuntimeException("Bestiary Manager not initialized");
		}
		return INSTANCE;
	}

	public static Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) {
		if (INSTANCE == null) {
			throw new RuntimeException("Bestiary Manager not initialized");
		}

		return INSTANCE.mStorage.getAllKilledMobs(player, searchSouls);
	}

	public static int getKillsForMob(Player player, SoulEntry soul) {
		if (INSTANCE == null) {
			throw new RuntimeException("Bestiary Manager not initialized");
		}

		return INSTANCE.mStorage.getKillsForMob(player, soul);
	}

	public static void setKillsForMob(Player player, SoulEntry soul, int amount) {
		if (INSTANCE == null) {
			throw new RuntimeException("Bestiary Manager not initialized");
		}

		INSTANCE.mStorage.setKillsForMob(player, soul, amount);
	}

	public static int addKillsToMob(Player player, SoulEntry soul, int amount) {
		if (INSTANCE == null) {
			throw new RuntimeException("Bestiary Manager not initialized");
		}

		return INSTANCE.mStorage.addKillsForMob(player, soul, amount);
	}

	public static void deleteAll(Player player) {
		List<SoulEntry> souls = SoulsDatabase.getInstance().getSouls();
		for (SoulEntry soul : souls) {
			setKillsForMob(player, soul, 0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		Entity damager = event.getDamager();
		if (entity instanceof LivingEntity &&
			!(entity instanceof Player) && damager instanceof Player) {
			// Non-player living entity was damaged by player

			if (entity.customName() != null) {
				// Damaged entity had a non-empty name

				Set<String> tags = entity.getScoreboardTags();
				if (!tags.isEmpty() && tags.contains("Boss")) {
					// Damaged entity has the Boss tag

					// Keep track of all players that damage this mob
					Set<UUID> damagers = mDamageTracker.get(entity.getUniqueId());
					if (damagers == null) {
						// Hasn't been damaged yet - create new set of damaging players
						damagers = new HashSet<>();
						damagers.add(damager.getUniqueId());
						mDamageTracker.put(entity.getUniqueId(), damagers);
					} else {
						// Has been damaged already - add player to set
						damagers.add(damager.getUniqueId());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityDeathEvent(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();

		Component name = entity.customName();
		if (name == null) {
			return;
		}

		Player player = entity.getKiller();
		if (player == null) {
			return;
		}

		// Player kills a mob
		SoulsDatabase database = SoulsDatabase.getInstance();
		if (database == null) {
			return;
		}

		try {
			String label = Utils.getLabelFromName(name);
			SoulEntry soul = database.getSoul(label);
			if (soul != null) {
				// A soul entry exists for this mob

				// Check if this was a boss with multiple tracked damagers
				Set<UUID> damagers = mDamageTracker.remove(entity.getUniqueId());
				if (damagers != null) {
					// A boss, record kill for everyone who damaged the mob (including the killer)
					damagers.add(player.getUniqueId());
					damagers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach((damager) -> {
						try {
							mStorage.recordKill(damager, soul);
						} catch (Exception ex) {
							mLogger.warning(ex.getMessage());
						}
					});
				} else {
					// Not a boss, record kill for the killer
					try {
						mStorage.recordKill(player, soul);
					} catch (Exception ex) {
						mLogger.warning(ex.getMessage());
					}

					// also check if any nearby player has not killed that mob before, and give them the kill
					// good for both unlocking the entry in group play, and also to stop descriptions from showing up
					// too many times
					List<Player> otherPlayers = player.getWorld().getPlayers();
					otherPlayers.remove(player);
					otherPlayers.removeIf(p -> p.getLocation().distanceSquared(player.getLocation()) > 20 * 20);
					otherPlayers.removeIf(p -> getKillsForMob(p, soul) > 0);
					for (Player p : otherPlayers) {
						try {
							mStorage.recordKill(p, soul);
						} catch (Exception ex) {
							mLogger.warning(ex.getMessage());
						}
					}
				}
			}
		} catch (Exception ex) {
			mLogger.warning(ex.getMessage());
		}
	}
}
