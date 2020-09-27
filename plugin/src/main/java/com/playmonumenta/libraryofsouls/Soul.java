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

	/* This is the full name, with colors, spaces, and possibly JSON */
	String getName();

	/* This is the label-ified name, with colors and spaces stripped */
	String getLabel();

	Entity summon(Location loc);
}
