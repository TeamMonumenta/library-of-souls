package com.playmonumenta.libraryofsouls;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;

public interface Soul {
	public NBTTagCompound getNBT();

	public long getModifiedOn();

	public String getModifiedBy();

	public ItemStack getPlaceholder();

	public ItemStack getBoS();

	/* This is the full raw mob name, with colors, spaces, and possibly JSON */
	public String getName();

	/* This is a color-adjusted LoS name with spaces and no JSON, colored white=normal gold if the Elite tag is present */
	public String getDisplayName();

	/* Whether the mob has the tag "Elite" or not */
	public boolean isElite();

	/* This is the label-ified name, with colors and spaces stripped */
	public String getLabel();

	public Entity summon(Location loc);
}
