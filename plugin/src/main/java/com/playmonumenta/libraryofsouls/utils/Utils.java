package com.playmonumenta.libraryofsouls.utils;

import org.bukkit.ChatColor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class Utils {
	private static Gson gson = new Gson();

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

	public static String getLabelFromName(String name) throws Exception {
		if (name == null) {
			return null;
		}

		String label = null;
		try {
			label = Utils.stripColorsAndJSON(gson, name).replaceAll("[^A-Za-z]", "");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to parse Library of Souls mob name '" + name + "'");
		}
		return label;
	}
}
