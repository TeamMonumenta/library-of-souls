/*
 * Copyright (C) 2013-2018 Gonçalo Baltazar <me@goncalomb.com>
 *
 * Ported from NBTEditor (https://github.com/goncalomb/NBTEditor) into
 * Library of Souls. Original package: com.goncalomb.bukkit.mylib.utils.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.playmonumenta.libraryofsouls.utils;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public final class UtilsMc {

	private static final Pattern TIME_DURATION_PATTERN = Pattern.compile(
		"^(?:(\\d{1,4})d)?(?:(\\d{1,2})h)?(?:(\\d{1,2})m)?(?:(\\d{1,2})s)?$",
		Pattern.CASE_INSENSITIVE);

	private static HashSet<Material> NON_TARGETABLE_BLOCKS = new HashSet<>();

	static {
		NON_TARGETABLE_BLOCKS.add(Material.AIR);
		for (Material mat : Material.values()) {
			if (mat.isBlock() && !mat.isSolid()) {
				NON_TARGETABLE_BLOCKS.add(mat);
			}
		}
		NON_TARGETABLE_BLOCKS.remove(Material.END_GATEWAY);
	}

	private UtilsMc() {
	}

	public static int parseTickDuration(String str) {
		int duration;
		try {
			duration = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			Matcher matcher = TIME_DURATION_PATTERN.matcher(str);
			if (!matcher.find()) {
				return -1;
			}
			int d = parseGroupInt(matcher.group(1));
			int h = parseGroupInt(matcher.group(2));
			int m = parseGroupInt(matcher.group(3));
			int s = parseGroupInt(matcher.group(4));
			if (h >= 24 || m >= 60 || s >= 60) {
				return -1;
			}
			duration = (d * 86400 + h * 3600 + m * 60 + s) * 20;
		}
		if (duration < 0) {
			return -1;
		}
		return duration;
	}

	private static int parseGroupInt(String group) {
		if (group == null) {
			return 0;
		}
		try {
			return Integer.parseInt(group);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Block getTargetBlock(Player player) {
		return getTargetBlock(player, 50);
	}

	public static Block getTargetBlock(Player player, int distance) {
		List<Block> blocks = player.getLastTwoTargetBlocks(NON_TARGETABLE_BLOCKS, distance);
		return blocks.get(blocks.size() - 1);
	}

	public static ItemStack newSingleItemStack(Material material, Component name) {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack newSingleItemStack(Material material, Component name, List<Component> lore) {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static boolean offsetItemStackDamage(ItemStack item, int offset) {
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable) {
			((Damageable) meta).setDamage(((Damageable) meta).getDamage() + offset);
			item.setItemMeta(meta);
			return true;
		}
		return false;
	}

	public static UUID convertFromUUIDInts(int[] uuidData) {
		return new UUID(((long) uuidData[0] << 32) | uuidData[1], ((long) uuidData[2] << 32) | uuidData[3]);
	}

	public static int[] convertToUUIDInts(UUID uuid) {
		return new int[] {
			(int)((uuid.getMostSignificantBits() >> 32) & 0xFFFFFFFF),
			(int)(uuid.getMostSignificantBits() & 0xFFFFFFFF),
			(int)((uuid.getLeastSignificantBits() >> 32) & 0xFFFFFFFF),
			(int)(uuid.getLeastSignificantBits() & 0xFFFFFFFF)
		};
	}

	public static Vector faceToDelta(BlockFace face) {
		return new Vector(1, 1, 1).add(new Vector(face.getModX(), face.getModY(), face.getModZ())).multiply(0.5);
	}

	public static Vector faceToDelta(BlockFace face, double distance) {
		Vector delta = faceToDelta(face);
		return new Vector(-0.5, -0.5, -0.5).add(delta).normalize().multiply(distance).add(delta);
	}

	public static void broadcastToWorld(World world, String message) {
		for (Player player : world.getPlayers()) {
			player.sendMessage(message);
		}
	}

}
