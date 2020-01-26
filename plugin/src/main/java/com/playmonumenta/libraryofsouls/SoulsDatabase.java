package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;

public class SoulsDatabase {
	private static final String SOULS_DATABASE_FILE = "souls_database.json";

	private static SoulsDatabase INSTANCE = null;

	private static final Comparator<String> COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String e1, String e2) {
			return e1.toLowerCase().compareTo(e2.toLowerCase());
		}
	};

	private final Plugin mPlugin;

	/* This is the primary database. One name, one SoulEntry per mob */
	private Map<String, SoulEntry> mSouls = new TreeMap<String, SoulEntry>(COMPARATOR);

	/*
	 * This is an index based on locations.
	 * A SoulEntry may appear here many times, or not at all
	 */
	private Map<String, List<SoulEntry>> mLocsIndex = new HashMap<String, List<SoulEntry>>();

	public SoulsDatabase(Plugin plugin) throws Exception {
		mPlugin = plugin;
		reload();

		INSTANCE = this;
	}

	public List<SoulEntry> getSoulsByLocation(String location) {
		return mLocsIndex.get(location);
	}

	public List<SoulEntry> getSouls() {
		List<SoulEntry> souls = new ArrayList<SoulEntry>(mSouls.size());
		souls.addAll(mSouls.values());
		return souls;
	}

	public SoulEntry getSoul(int index) {
		if (index >= mSouls.size()) {
			return null;
		}

		return (SoulEntry)mSouls.values().toArray()[index];
	}

	public SoulEntry getSoul(String name) {
		SoulEntry soul = mSouls.get(name);
		if (soul != null) {
			return soul;
		}
		return null;
	}

	public boolean del(CommandSender sender, String name) {
		if (mSouls.containsKey(name)) {
			mSouls.remove(name);
			sender.sendMessage(ChatColor.GREEN + "Removed " + name);
			save();
			return true;
		}

		sender.sendMessage(ChatColor.RED + "Mob '" + name + "' does not exist!");
		return false;
	}

	/* TODO: File watcher */
	public void reload() throws Exception {
		mPlugin.getLogger().info("Parsing souls library...");
		mSouls = new TreeMap<String, SoulEntry>(COMPARATOR);

		File directory = mPlugin.getDataFolder();
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String content = FileUtils.readFile(Paths.get(mPlugin.getDataFolder().getPath(), SOULS_DATABASE_FILE).toString());
		if (content == null || content.isEmpty()) {
			throw new Exception("Failed to parse file as JSON object");
		}

		Gson gson = new Gson();
		JsonArray array = gson.fromJson(content, JsonArray.class);
		if (array == null) {
			throw new Exception("Failed to parse file as JSON array");
		}

		int count = 0;
		Iterator<JsonElement> iter = array.iterator();
		while (iter.hasNext()) {
			JsonElement entry = iter.next();

			JsonObject obj = entry.getAsJsonObject();

			SoulEntry soul = new SoulEntry(obj);
			String label = soul.getLabel();

			if (mSouls.get(label) != null) {
				mPlugin.getLogger().severe("Refused to load Library of Souls duplicate mob '" + label + "'");
				continue;
			}

			mPlugin.getLogger().info("  " + label);

			mSouls.put(label, soul);

			for (String tag : soul.getLocationNames()) {
				List<SoulEntry> lst = mLocsIndex.get(tag);
				if (lst == null) {
					lst = new LinkedList<SoulEntry>();
					mLocsIndex.put(tag, lst);
				}
				lst.add(soul);
			}
			count++;
		}
		mPlugin.getLogger().info("Finished parsing souls library");
		mPlugin.getLogger().info("Loaded " + Integer.toString(count) + " mob souls");
	}

	// TODO: Private
	public void save() {
		JsonArray array = new JsonArray();
		for (SoulEntry soul : mSouls.values()) {
			array.add(soul.serialize());
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String path = Paths.get(mPlugin.getDataFolder().getPath(), SOULS_DATABASE_FILE).toString();

		try {
			FileUtils.writeFile(path, gson.toJson(array));
		} catch (Exception ex) {
			mPlugin.getLogger().severe("Failed to save souls database to '" + path + "': " + ex.getMessage());
		}
	}

	public static SoulsDatabase getInstance() {
		return INSTANCE;
	}

	public Set<String> listMobNames() {
		return mSouls.keySet();
	}

	public Set<String> listMobLocations() {
		return mLocsIndex.keySet();
	}
}
