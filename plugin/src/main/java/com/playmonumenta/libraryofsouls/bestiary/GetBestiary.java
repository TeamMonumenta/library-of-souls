package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;


public class GetBestiary {
	private static String mPage = "'";
	public static void regenerateBook() {
		//Dont trust any of this
		int count = 0;
		JsonArray page = new JsonArray();
		JsonObject text = new JsonObject();
		text.addProperty("text", "");
		JsonObject newLine = new JsonObject();
		newLine.addProperty("text", "\\n");
		for (String location : BestiaryUtils.mBookMap) {
			JsonArray extra = new JsonArray();
			JsonObject ultraEntry = new JsonObject();
			JsonObject entry = new JsonObject();

			entry.addProperty("text", BestiaryUtils.formatWell(location));
			entry.addProperty("color", generateColor(location));

			JsonObject clickEvent = new JsonObject();
			clickEvent.addProperty("action", "run_command");
			clickEvent.addProperty("value", "/bestiary " + location);

			entry.add("clickEvent", clickEvent);
			page.add(entry);
			page.add(newLine);
			page.add(newLine);
			count++;
			if (count % 6 == 0 || count == BestiaryUtils.mBookMap.size()) {
			page.add(text);
			extra.add(page);
			page = new JsonArray();
			ultraEntry.add("extra", extra);
			mPage += count != BestiaryUtils.mBookMap.size() ? ultraEntry.toString().substring(0, ultraEntry.toString().length() - 2) + "],\"text\":\"\"}','" : ultraEntry.toString();
			}
		}
		BestiaryUtils.registerPoiLocs();
	}

	public static void getBook(Player player) {
		if (mPage == null) {
			LibraryOfSouls.getInstance().getLogger().warning("Requested book for player " + player.getName() + " but bestiary has not been generated yet");
			return;
		}
		String commandString = mPage;
//		Bukkit.broadcastMessage("give @s minecraft:written_book{pages:[" + commandString.substring(0, commandString.length() - 2) + ",\"text\":\"\"}'],\"title\":\"Bestiary\",\"author\":\"Fred\",resolved:1b}");
		Bukkit.getConsoleSender().getServer().dispatchCommand(player, "give @s minecraft:written_book{pages:[" + commandString.substring(0, commandString.length() - 2) + "],\"text\":\"\"}'],\"title\":\"Bestiary\",\"author\":\"Fred\",display:{Name:'{\"text\":\"Bestiary\"}'},resolved:1b}");
	}
	private static String generateColor(String in) {
		switch(in) {
		case "labs":
			return "black";
		case "willows":
			return "green";
		case "roguelike":
			return "red";
		case "reverie":
			return "red";
		case "shiftingcity":
			return "aqua";
		case "region1":
			return "black";
		case "region2":
			return "black";
		case "lightblue":
			return "blue";
		case "lightgray":
			return "gray";
		case "gray":
			return "dark_gray";
		case "orange":
			return "gold";
		case "magenta":
			return "light_purple";
		case "yellow":
			return "yellow";
		case "lime":
			return "green";
		case "pink":
			return "red";
		case "cyan":
			return "dark_aqua";
		case "purple":
			return "dark_purple";
		default:
			return "black";
		}
	}
}
