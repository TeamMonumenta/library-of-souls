package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


public class GetBestiary {
	private static ItemStack mBestiary = new ItemStack(Material.ENCHANTED_BOOK);

	static {
		ItemMeta meta = mBestiary.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Bestiary");
		List<String> lore = new ArrayList<>();
		lore.add("Written by: Erwen");
		lore.add(ChatColor.GRAY + "A compendium of every manner of beasts");
		lore.add(ChatColor.GRAY + "from across land and sea,");
		lore.add(ChatColor.GRAY + "passed from adventurer to adventurer");
		lore.add(ChatColor.GRAY + "throughout the ages.");
	}

	public static void getBook(Player player) {
		World world = player.getWorld();
		Item itemEntity = world.dropItem(player.getLocation(), mBestiary);
		itemEntity.setPickupDelay(0);
	}
}
