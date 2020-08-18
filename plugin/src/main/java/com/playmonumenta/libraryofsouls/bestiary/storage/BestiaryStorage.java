package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.Map;

import com.playmonumenta.libraryofsouls.SoulEntry;

import org.bukkit.entity.Player;

public interface BestiaryStorage {
	/** Records when a player has killed the given mob */
	public void recordKill(Player player, SoulEntry soul);

	/** Given a set of Souls, get a map of how many of each have been killed by the player */
	Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls);
}
