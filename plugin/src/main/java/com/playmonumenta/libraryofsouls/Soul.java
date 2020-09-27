package com.playmonumenta.libraryofsouls;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;

public interface Soul {
	NBTTagCompound getNBT();

	long getModifiedOn();

	String getModifiedBy();

	ItemStack getPlaceholder();

	ItemStack getBoS();

	/* This is the full raw mob name, with colors, spaces, and possibly JSON */
	String getName();

	/* This is a color-adjusted LoS name with spaces and no JSON, colored white=normal gold if the Elite tag is present */
	String getDisplayName();

	/* Whether the mob has the tag "Elite" or not */
	boolean isElite();

	/* This is the label-ified name, with colors and spaces stripped */
	String getLabel();

	Entity summon(Location loc);
}
