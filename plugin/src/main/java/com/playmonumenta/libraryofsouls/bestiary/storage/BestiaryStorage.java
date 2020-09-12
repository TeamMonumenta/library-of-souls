package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.Map;

import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.SoulEntry;

public interface BestiaryStorage {
	/** Records when a player has killed the given mob */
	public void recordKill(Player player, SoulEntry soul);

	public int getKillsForMob(Player player, String name);

	public boolean setKillsForMob(Player player, String name, int amount);

	public boolean addKillsToMob(Player player, String name, int amount);

	/** Given a set of Souls, get a map of how many of each have been killed by the player */
	Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls);
}
