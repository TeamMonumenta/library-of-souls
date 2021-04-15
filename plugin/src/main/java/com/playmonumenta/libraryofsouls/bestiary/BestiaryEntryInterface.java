package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

public interface BestiaryEntryInterface {
	/* Name of this bestiary entry for display purposes */
	Component getName();

	/* Checks whether the player has permission to open this bestiary entry */
	boolean canOpenBestiary(Player player);

	/* Gets the item that would be clicked on to open this entry */
	ItemStack getBestiaryItem(Player player);

	/* Open the contents of this entry for a specific player
	 * Note that parent must be provided here so a mob bestiary page can get
	 * back to the index page that accessed it
	 */
	void openBestiary(Player player, BestiaryArea parent);
}
