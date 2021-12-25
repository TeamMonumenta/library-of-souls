package com.playmonumenta.libraryofsouls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;
import com.playmonumenta.libraryofsouls.utils.Utils;

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
	private final Path mSoulsDatabasePath;
	private long mPreviousReloadMs = 0;
	private boolean mIgnoreNextChange = false;

	/* This is the primary database. One name, one SoulEntry per mob */
	private Map<String, SoulEntry> mSouls = new TreeMap<String, SoulEntry>(COMPARATOR);

	/*
	 * This is an index based on locations.
	 * A SoulEntry may appear here many times, or not at all
	 */
	private final Map<String, List<SoulEntry>> mLocsIndex = new HashMap<>();
	private final List<SoulEntry> mNoLocMobs = new ArrayList<>();
	/*
	 * This is an index based on mob ID (zombie, skeleton, etc.)
	 * A SoulEntry may appear here many times
	 */
	private final Map<String, List<SoulEntry>> mTypesIndex = new HashMap<>();

	public SoulsDatabase(Plugin plugin) throws Exception {
		mPlugin = plugin;
		mSoulsDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOULS_DATABASE_FILE);

		/* Periodically check the file to see if it has changed */
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			try {
				mPlugin.getLogger().fine("Polling souls database file...");
				long lastModMs = Files.getLastModifiedTime(mSoulsDatabasePath).toMillis();
				if (lastModMs > mPreviousReloadMs) {
					/* File has changed since we read it */
					if (!mIgnoreNextChange) {
						/* This change wasn't expected - reload the database */
						reloadAsync();
					}

					mPreviousReloadMs = lastModMs;
					mIgnoreNextChange = false;
				}
			} catch (Exception e) {
				mPlugin.getLogger().warning("Caught exception while polling database file: " + e.getMessage());
				e.printStackTrace();
			}
		}, 200, 200);

		INSTANCE = this;
	}

	public List<SoulEntry> getSoulsByLocation(String location) {
		if (location == null) {
			return mNoLocMobs;
		} else {
			return mLocsIndex.get(location);
		}
	}

	public List<SoulEntry> getSoulsByType(String id) {
		return mTypesIndex.get(id);
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

	/*################################################################################
	 * Functions that change the database state
	 */

	public void add(Player sender, BookOfSouls bos) {
		SoulEntry soul;

		try {
			NBTTagCompound nbt = bos.getEntityNBT().getData();
			String name = nbt.getString("CustomName");
			String label = Utils.getLabelFromName(name);

			if (mSouls.containsKey(label)) {
				sender.sendMessage(ChatColor.RED + "Mob '" + label + "' already exists!");
				return;
			}

			soul = new SoulEntry(sender, nbt);
		} catch (Exception ex) {
			sender.sendMessage(ChatColor.RED + "Error parsing BoS: " + ex.getMessage());
			return;
		}

		mSouls.put(soul.getLabel(), soul);
		sender.sendMessage(ChatColor.GREEN + "Added " + soul.getLabel());
		updateIndex();
		save();
	}

	public void update(Player sender, BookOfSouls bos) {
		SoulEntry soul;

		try {
			NBTTagCompound nbt = bos.getEntityNBT().getData();
			String name = nbt.getString("CustomName");
			String label = Utils.getLabelFromName(name);

			soul = mSouls.get(label);
			if (soul == null) {
				sender.sendMessage(ChatColor.RED + "Mob '" + label + "' does not exist!");
				return;
			}

			soul.update(sender, nbt);
		} catch (Exception ex) {
			sender.sendMessage(ChatColor.RED + "Error parsing BoS: " + ex.getMessage());
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Updated " + soul.getLabel());
		updateIndex();
		save();
	}

	// This function is only called in updateLore, where by definition the soul exists - also the bos doesnt change internally, only on the outside but maybe that needs to happen?
	public void updateLore(SoulEntry soul, Player sender) {
		try {
			soul.update(sender, soul.getNBT());
		} catch (Exception ex) {
			sender.sendMessage("Exception when updating lore: " + ex + " for " + soul.getDisplayName());
		}
		save();
	}

	public void del(CommandSender sender, String name) {
		if (!mSouls.containsKey(name)) {
			sender.sendMessage(ChatColor.RED + "Mob '" + name + "' does not exist!");
		} else {
			mSouls.remove(name);
			sender.sendMessage(ChatColor.GREEN + "Removed " + name);
			updateIndex();
			save();
		}
	}

	public void reloadAsync() throws Exception {
		mPlugin.getLogger().info("Reloading souls library...");
		Map<String, SoulEntry> newSouls = new TreeMap<>(COMPARATOR);

		String content = FileUtils.readFile(mSoulsDatabasePath.toString());
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

			SoulEntry soul = SoulEntry.fromJson(obj);
			String label = soul.getLabel();

			if (newSouls.get(label) != null) {
				mPlugin.getLogger().severe("Refused to load Library of Souls duplicate mob '" + label + "'");
				continue;
			}

			mPlugin.getLogger().fine("  " + label);

			newSouls.put(label, soul);

			count++;
		}

		final int finalCount = count;
		Bukkit.getScheduler().runTask(mPlugin, () -> {
			mSouls = newSouls;
			updateIndex();

			/* Reload the main plugin config / bestiary also after reloading the database */
			LibraryOfSouls.Config.load(mPlugin.getLogger(), mPlugin.getDataFolder());

			mPlugin.getLogger().info("Finished parsing souls library");
			mPlugin.getLogger().info("Loaded " + Integer.toString(finalCount) + " mob souls");
		});
	}

	/*
	 * Functions that change the database state
	 *################################################################################*/

	private void updateIndex() {
		mLocsIndex.clear();
		mNoLocMobs.clear();
		mTypesIndex.clear();
		for (SoulEntry soul : mSouls.values()) {
			/* Update location index */
			Set<String> locs = soul.getLocationNames();
			if (locs == null || locs.isEmpty()) {
				mNoLocMobs.add(soul);
			} else {
				for (String tag : locs) {
					List<SoulEntry> lst = mLocsIndex.get(tag);
					if (lst == null) {
						lst = new ArrayList<SoulEntry>();
						mLocsIndex.put(tag, lst);
					}
					lst.add(soul);
				}
			}

			/* Update type index */
			String id = soul.getId().getKey().toLowerCase();
			List<SoulEntry> lst = mTypesIndex.get(id);
			if (lst == null) {
				lst = new ArrayList<SoulEntry>();
				mTypesIndex.put(id, lst);
			}
			lst.add(soul);
		}
	}

	private void save() {
		JsonArray array = new JsonArray();
		for (SoulEntry soul : mSouls.values()) {
			array.add(soul.toJson());
		}

		/* Mark the next change as expected so the watcher doesn't reload it */
		mIgnoreNextChange = true;
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

	public Set<String> listMobTypes() {
		return mTypesIndex.keySet();
	}
}
