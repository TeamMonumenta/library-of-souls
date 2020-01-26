package com.playmonumenta.libraryofsouls.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.command.MyCommandException;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class Utils {
	/*
	 * Valid examples:
	 *   §6Master Scavenger
	 *   "§6Master Scavenger"
	 *   "{\"text\":\"§6Master Scavenger\"}"
	 */
	public static String stripColorsAndJSON(Gson gson, String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		JsonElement element = gson.fromJson(str, JsonElement.class);
		return stripColorsAndJSON(element);
	}

	public static String stripColorsAndJSON(JsonElement element) {
		String str = "";
		if (element.isJsonObject()) {
			JsonElement textElement = element.getAsJsonObject().get("text");
			if (textElement != null) {
				str = textElement.getAsString();
			}
		} else if (element.isJsonArray()) {
			str = "";
			for (JsonElement arrayElement : element.getAsJsonArray()) {
				str += stripColorsAndJSON(arrayElement);
			}
		} else {
			str = element.getAsString();
		}
		return ChatColor.stripColor(str);
	}

	public static String hashColor(String in) {
		int val = in.hashCode() % 13;
		switch (val) {
			case 0:
				return ChatColor.DARK_GREEN + in;
			case 1:
				return ChatColor.DARK_AQUA + in;
			case 2:
				return ChatColor.DARK_RED + in;
			case 3:
				return ChatColor.DARK_PURPLE + in;
			case 4:
				return ChatColor.GOLD + in;
			case 5:
				return ChatColor.GRAY + in;
			case 6:
				return ChatColor.DARK_GRAY + in;
			case 7:
				return ChatColor.BLUE + in;
			case 8:
				return ChatColor.GREEN + in;
			case 9:
				return ChatColor.AQUA + in;
			case 10:
				return ChatColor.RED + in;
			case 11:
				return ChatColor.LIGHT_PURPLE + in;
			default:
				return ChatColor.YELLOW + in;
		}
	}

	public static BookOfSouls getBos(Player player) throws MyCommandException {
		return getBos(player, false);
	}

	public static BookOfSouls getBos(Player player, boolean nullIfMissing) throws MyCommandException {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (BookOfSouls.isValidBook(item)) {
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos != null) {
				return bos;
			}
			throw new MyCommandException("§cThat Book of Souls is corrupted!");
		} else if (!nullIfMissing) {
			throw new MyCommandException("§cYou must be holding a Book of Souls!");
		}
		return null;
	}
}
