package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import net.md_5.bungee.api.ChatColor;

public class GetBestiary {
	private static Set<String> mLocs;
	private static BookMeta mBestiary = null;

	public static void regenerateBook() {
		mLocs = SoulsDatabase.getInstance().listMobLocations();
		int count = 0;
		int pageNum = 1;
		String page = "pages:[\"[\"\",";
		for (String location : mLocs) {
			page += "{\"text\":\""+ location + "\",\"color\":\"" + generateColor(location) + "\",\"insertion\":\"" + location + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/beastiary " + location + "\"}},{\"text\":\"\\n\"},";
			count++;
			if (count == 5) {
				count = 0;
				page = page.substring(0, page.length() - 2);
				mBestiary.setPage(pageNum, "Hat");
				pageNum++;
				page = "";
			}
		}
		mBestiary.setTitle("Bestiary");
		mBestiary.setGeneration(Generation.ORIGINAL);
		mBestiary.setAuthor("Fred");
	}

	public static void getBook(Player player) {
		if (mBestiary == null) {
			LibraryOfSouls.getInstance().getLogger().warning("Requested book for player " + player.getName() + " but bestiary has not been generated yet");
			return;
		}

		World world = player.getWorld();
		ItemStack bestiary = new ItemStack(Material.WRITTEN_BOOK);
		bestiary.setItemMeta(mBestiary);
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Bestiary");
		bestiary.getItemMeta().setLore(lore);
		Item item = world.dropItem(player.getLocation(), bestiary);
		item.setPickupDelay(0);
	}

	private static String generateColor(String in) {
		switch (in) {
		case "white":
			return "white";
		case "orange":
			return "orange";
		case "magenta":
			return "light_purple";
		case "light_blue":
			return "blue";
		case "yellow":
			return "yellow";
		case "lime":
			return "green";
		case "pink":
			return "red";
		case "gray":
			return "dark_gray";
		case "light_gray":
			return "gray";
		case "cyan":
			return "dark_aqua";
		case "purple":
			return "dark_purple";
		case "blue":
			return "dark_blue";
		case "brown":
			return "aqua";
		case "green":
			return "dark_green";
		case "red":
			return "dark_red";
		case "black":
			return "black";
		case "shiftingcity":
			return "aqua";
		}
		return "black";
	}
}
