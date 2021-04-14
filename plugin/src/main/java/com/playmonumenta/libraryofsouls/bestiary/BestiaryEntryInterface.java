package com.playmonumenta.libraryofsouls.bestiary;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BestiaryEntryInterface {
	/* Name of this bestiary entry for display purposes */
	String getName();

	/* Checks whether the player has permission to open this bestiary entry */
	boolean canOpenBestiary(Player player);

	/* Gets the item that would be clicked on to open this entry */
	ItemStack getBestiaryItem(Player player);

	/* Get the parent that contains this entry */
	BestiaryEntryInterface getBestiaryParent();

	/* Open the contents of this entry for a specific player
	 * Note that parent must be provided here so a mob bestiary page can get
	 * back to the index page that accessed it
	 */
	void openBestiary(Player player, BestiaryEntryInterface parent);
}
