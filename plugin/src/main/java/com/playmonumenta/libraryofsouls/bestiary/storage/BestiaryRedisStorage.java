package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.playmonumenta.libraryofsouls.SoulEntry;

import org.bukkit.entity.Player;

/* TODO:
 * This doesn't currently actually store data in Redis. Data is lost when the server restarts.
 *
 * This will need the plugin data storage api implemented in Redis before it can be finished.
 *
 * It does however keep track of kills while the server is running
 */

public class BestiaryRedisStorage implements BestiaryStorage {
	private final HashMap<UUID, Map<SoulEntry, Integer>> mPlayerKills = new HashMap<>();

	@Override
	public void recordKill(Player player, SoulEntry soul) {
		Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());
		if (playerKills == null) {
			playerKills = new HashMap<>();
			mPlayerKills.put(player.getUniqueId(), playerKills);
		}

		Integer kills = playerKills.get(soul);
		if (kills == null) {
			playerKills.put(soul, 1);
		} else  {
			playerKills.put(soul, kills + 1);
		}
	}

	@Override
	public Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) {
		Map<SoulEntry, Integer> map = new HashMap<>();
		Map<SoulEntry, Integer> playerKills = mPlayerKills.get(player.getUniqueId());

		for (SoulEntry soul : searchSouls) {
			if (playerKills == null) {
				map.put(soul, 0);
			} else {
				map.put(soul, playerKills.getOrDefault(soul, 0));
			}
		}

		return map;
	}
}
