package com.playmonumenta.libraryofsouls.bestiary.storage;

import java.util.Collection;
import java.util.Map;

import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface BestiaryStorage {
	/** Loads data for a given player (such as at login) */
	public void load(Plugin plugin, Player player, SoulsDatabase database);

	/** Saves data for a given player (such as at logout) */
	public void save(Player player, SoulsDatabase database);

	/** Records when a player has killed the given mob */
	public void recordKill(Player player, SoulEntry soul) throws Exception;

	/** Gets the number of times a player has killed the given mob */
	public int getKillsForMob(Player player, SoulEntry soul) throws Exception;

	/** Sets the number of times a player has killed the given mob */
	public int setKillsForMob(Player player, SoulEntry soul, int amount) throws Exception;

	/** Adds to the number of times a player has killed the given mob */
	public int addKillsForMob(Player player, SoulEntry soul, int amount) throws Exception;

	/** Given a set of Souls, get a map of how many of each have been killed by the player */
	Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls) throws Exception;
}
