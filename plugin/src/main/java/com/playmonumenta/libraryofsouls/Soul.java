package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public interface Soul {
	NBTTagCompound getNBT();

	long getModifiedOn();

	String getModifiedBy();

	ItemStack getPlaceholder();

	ItemStack getBoS();

	/* NamespacedKey of the mob (i.e. minecraft:zombie, etc.) */
	NamespacedKey getId();

	/* This is the full raw mob name, with colors, spaces, and possibly JSON */
	String getName();

	/* This is a color-adjusted LoS name with spaces and no JSON, colored white=normal gold if the Elite tag is present */
	String getDisplayName();

	/* Whether the mob has the tag "Boss" or not */
	boolean isBoss();

	/* Whether the mob has the tag "Elite" or not */
	boolean isElite();

	/* This is the label-ified name, with colors and spaces stripped */
	String getLabel();

	Entity summon(Location loc);
}
