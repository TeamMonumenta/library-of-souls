package com.playmonumenta.libraryofsouls;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;

public interface Soul {
	public NBTTagCompound getNBT();

	public long getModifiedOn();

	public String getModifiedBy();

	public ItemStack getPlaceholder();

	public ItemStack getBoS();

	/* This is the full name, with colors, spaces, and possibly JSON */
	public String getName();

	/* This is the label-ified name, with colors and spaces stripped */
	public String getLabel();

	public void summon(Location loc);
}
