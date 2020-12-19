package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.Map;

import com.playmonumenta.libraryofsouls.SoulEntry;

import org.bukkit.entity.Player;

public interface BestiaryStorage {
	/** Records when a player has killed the given mob */
	void recordKill(Player player, SoulEntry soul) throws Exception;

	/** Gets the number of times a player has killed the given mob */
	int getKillsForMob(Player player, SoulEntry soul) throws Exception;

	/** Sets the number of times a player has killed the given mob */
	int setKillsForMob(Player player, SoulEntry soul, int amount) throws Exception;

	/** Adds to the number of times a player has killed the given mob */
	int addKillsForMob(Player player, SoulEntry soul, int amount) throws Exception;

	/** Given a set of Souls, get a map of how many of each have been killed by the player */
	Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) throws Exception;
}
