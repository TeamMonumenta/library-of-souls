package com.playmonumenta.libraryofsouls;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;

import net.kyori.adventure.text.Component;

public interface Soul extends SoulGroup {
	NBTTagCompound getNBT();

	ItemStack getPlaceholder();

	ItemStack getBoS();

	/* NamespacedKey of the mob (i.e. minecraft:zombie, etc.) */
	NamespacedKey getId();

	/* This is the full raw mob name, with colors, spaces, and possibly JSON */
	Component getName();

	/* This is a color-adjusted LoS name with spaces and no JSON, colored white=normal gold if the Elite tag is present */
	Component getDisplayName();

	/* Whether the mob has the tag "Boss" or not */
	boolean isBoss();

	/* Whether the mob has the tag "Elite" or not */
	boolean isElite();

	Entity summon(Location loc);
}
