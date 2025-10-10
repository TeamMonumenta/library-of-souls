package com.playmonumenta.libraryofsouls.bestiary.storage;

import com.playmonumenta.libraryofsouls.SoulEntry;
import java.util.Collection;
import java.util.Map;
import org.bukkit.entity.Player;

public interface BestiaryStorage {
	/** Records when a player has killed the given mob */
	void recordKill(Player player, SoulEntry soul);

	/** Gets the number of times a player has killed the given mob */
	int getKillsForMob(Player player, SoulEntry soul);

	/** Sets the number of times a player has killed the given mob */
	void setKillsForMob(Player player, SoulEntry soul, int amount);

	/** Adds to the number of times a player has killed the given mob */
	int addKillsForMob(Player player, SoulEntry soul, int amount);

	/** Given a set of Souls, get a map of how many of each have been killed by the player */
	Map<SoulEntry, Integer> getAllKilledMobs(Player player, Collection<SoulEntry> searchSouls);
}
