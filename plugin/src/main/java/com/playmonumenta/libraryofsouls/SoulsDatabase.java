package com.playmonumenta.libraryofsouls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;
import com.playmonumenta.libraryofsouls.utils.Utils;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulsDatabase {
	private static final String SOULS_DATABASE_FILE = "souls_database.json";
	private static final String SOUL_PARTIES_DATABASE_FILE = "soul_parties_database.json";
	private static final String SOUL_POOLS_DATABASE_FILE = "soul_pools_database.json";

	private static SoulsDatabase INSTANCE = null;

	private static final Comparator<String> COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String e1, String e2) {
			return e1.toLowerCase().compareTo(e2.toLowerCase());
		}
	};

	private final Plugin mPlugin;
	private final Path mSoulsDatabasePath;
	private final Path mSoulPartiesDatabasePath;
	private final Path mSoulPoolsDatabasePath;
	private long mPreviousReloadMs = 0;
	private boolean mIgnoreNextChange = false;

	/* This is the primary database. One name, one SoulEntry per mob */
	private Map<String, SoulEntry> mSouls = new TreeMap<String, SoulEntry>(COMPARATOR);

	/* These are secondary databases for pools/parties of mobs */
	private Map<String, SoulPartyEntry> mSoulParties = new TreeMap<>(COMPARATOR);
	private Map<String, SoulPoolEntry> mSoulPools = new TreeMap<>(COMPARATOR);

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
		mSoulPartiesDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOUL_PARTIES_DATABASE_FILE);
		mSoulPoolsDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOUL_POOLS_DATABASE_FILE);

		/* Periodically check the file to see if it has changed */
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			try {
				mPlugin.getLogger().fine("Polling souls database file...");
				long lastModMs = Files.getLastModifiedTime(mSoulsDatabasePath).toMillis();
				long lastModMsParties = Files.getLastModifiedTime(mSoulPartiesDatabasePath).toMillis();
				long lastModMsPools = Files.getLastModifiedTime(mSoulPoolsDatabasePath).toMillis();
				if (lastModMs > mPreviousReloadMs
				    || lastModMsParties > mPreviousReloadMs
				    || lastModMsPools > mPreviousReloadMs) {
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

	public void autoUpdate(CommandSender sender, Location loc) {
		Iterator<Map.Entry<String, SoulEntry>> it = mSouls.entrySet().iterator();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (it.hasNext()) {
					Map.Entry<String, SoulEntry> entry = it.next();
					String name = entry.getKey();
					SoulEntry soulEntry = entry.getValue();
					try {
						soulEntry.autoUpdate(loc);
					} catch (Exception ex) {
						sender.sendMessage(ChatColor.RED + "Failed to auto-update " + name + ": " + ex.getMessage());
					}
				} else {
					sender.sendMessage(ChatColor.GRAY + "Auto-update done.");
					cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0L, 1L);
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
		return mSouls.get(name);
	}

	public List<SoulPartyEntry> getSoulParties() {
		List<SoulPartyEntry> soulParties = new ArrayList<SoulPartyEntry>(mSoulParties.size());
		soulParties.addAll(mSoulParties.values());
		return soulParties;
	}

	public SoulPartyEntry getSoulParty(int index) {
		if (index >= mSoulParties.size()) {
			return null;
		}

		return (SoulPartyEntry)mSoulParties.values().toArray()[index];
	}

	public SoulPartyEntry getSoulParty(String label) {
		SoulPartyEntry soulParty = mSoulParties.get(label);
		return soulParty;
	}

	public List<SoulPoolEntry> getSoulPools() {
		List<SoulPoolEntry> soulPools = new ArrayList<SoulPoolEntry>(mSoulPools.size());
		soulPools.addAll(mSoulPools.values());
		return soulPools;
	}

	public SoulPoolEntry getSoulPool(int index) {
		if (index >= mSoulPools.size()) {
			return null;
		}

		return (SoulPoolEntry)mSoulPools.values().toArray()[index];
	}

	public SoulPoolEntry getSoulPool(String label) {
		return mSoulPools.get(label);
	}

	public SoulGroup getSoulGroup(String label) {
		if (label.startsWith(LibraryOfSoulsAPI.SOUL_PARTY_PREFIX)) {
			return mSoulParties.get(label);
		} else if (label.startsWith(LibraryOfSoulsAPI.SOUL_POOL_PREFIX)) {
			return mSoulPools.get(label);
		} else {
			return mSouls.get(label);
		}
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

	public void addParty(Player player, String label) {
		SoulPartyEntry soulParty = new SoulPartyEntry(player, label);

		mSoulParties.put(soulParty.getLabel(), soulParty);
		player.sendMessage(ChatColor.GREEN + "Added " + soulParty.getLabel());
		save();
	}

	public void updateParty(Player player, String label, String entryLabel, int count) throws WrapperCommandSyntaxException {
		SoulPartyEntry soulParty = getSoulParty(label);
		if (soulParty == null) {
			CommandAPI.fail("Soul party '" + label + "' does not exist!");
		}

		SoulGroup soulGroup = getSoulGroup(entryLabel);
		if (soulGroup == null) {
			CommandAPI.fail("Soul group '" + entryLabel + "' does not exist!");
		}

		soulParty.update(player, entryLabel, count);
		player.sendMessage(ChatColor.GREEN + "Updated " + soulParty.getLabel());
		save();
	}

	public void delParty(CommandSender player, String label) {
		if (!mSoulParties.containsKey(label)) {
			player.sendMessage(ChatColor.RED + "Soul party '" + label + "' does not exist!");
		} else {
			mSoulParties.remove(label);
			player.sendMessage(ChatColor.GREEN + "Removed " + label);
			save();
		}
	}

	public void addPool(Player player, String label) {
		SoulPoolEntry soulPool = new SoulPoolEntry(player, label);

		mSoulPools.put(soulPool.getLabel(), soulPool);
		player.sendMessage(ChatColor.GREEN + "Added " + soulPool.getLabel());
		save();
	}

	public void updatePool(Player player, String label, String entryLabel, int weight) throws WrapperCommandSyntaxException {
		SoulPoolEntry soulPool = getSoulPool(label);
		if (soulPool == null) {
			CommandAPI.fail("Soul Pool '" + label + "' does not exist!");
		}

		SoulGroup soulGroup = getSoulGroup(entryLabel);
		if (soulGroup == null) {
			CommandAPI.fail("Soul group '" + entryLabel + "' does not exist!");
		}

		soulPool.update(player, entryLabel, weight);
		player.sendMessage(ChatColor.GREEN + "Updated " + soulPool.getLabel());
		save();
	}

	public void delPool(CommandSender player, String label) {
		if (!mSoulPools.containsKey(label)) {
			player.sendMessage(ChatColor.RED + "Soul Pool '" + label + "' does not exist!");
		} else {
			mSoulPools.remove(label);
			player.sendMessage(ChatColor.GREEN + "Removed " + label);
			save();
		}
	}

	public void reloadAsync() throws Exception {
		mPlugin.getLogger().info("Reloading souls library...");
		Map<String, SoulEntry> newSouls = new TreeMap<>(COMPARATOR);
		Map<String, SoulPartyEntry> newSoulParties = new TreeMap<>(COMPARATOR);
		Map<String, SoulPoolEntry> newSoulPools = new TreeMap<>(COMPARATOR);

		String content = FileUtils.readFile(mSoulsDatabasePath.toString());
		if (content == null || content.isEmpty()) {
			throw new Exception("Failed to read souls database");
		}

		Gson gson = new Gson();
		JsonArray soulsArray = gson.fromJson(content, JsonArray.class);
		if (soulsArray == null) {
			throw new Exception("Failed to parse souls database as JSON array");
		}

		content = FileUtils.readFile(mSoulPartiesDatabasePath.toString());
		if (content == null || content.isEmpty()) {
			throw new Exception("Failed to read soul parties database");
		}

		JsonArray soulPartiesArray = gson.fromJson(content, JsonArray.class);
		if (soulPartiesArray == null) {
			throw new Exception("Failed to parse soul parties database as JSON array");
		}

		content = FileUtils.readFile(mSoulPoolsDatabasePath.toString());
		if (content == null || content.isEmpty()) {
			throw new Exception("Failed to read soul parties database");
		}

		JsonArray soulPoolsArray = gson.fromJson(content, JsonArray.class);
		if (soulPoolsArray == null) {
			throw new Exception("Failed to parse soul pools database as JSON array");
		}

		mPlugin.getLogger().info("Souls:");
		int soulCount = 0;
		for (JsonElement entry : soulsArray) {
			JsonObject obj = entry.getAsJsonObject();

			SoulEntry soul = SoulEntry.fromJson(obj);
			String label = soul.getLabel();

			if (newSouls.get(label) != null) {
				mPlugin.getLogger().severe("Refused to load Library of Souls duplicate mob '" + label + "'");
				continue;
			}

			mPlugin.getLogger().fine("  " + label);

			newSouls.put(label, soul);

			soulCount++;
		}

		mPlugin.getLogger().info("Soul parties:");
		int soulPartyCount = 0;
		for (JsonElement entry : soulPartiesArray) {
			JsonObject obj = entry.getAsJsonObject();

			SoulPartyEntry soulParty = SoulPartyEntry.fromJson(obj);
			String label = soulParty.getLabel();

			if (newSoulParties.get(label) != null) {
				mPlugin.getLogger().severe("Refused to load Library of Souls duplicate soul party '" + label + "'");
				continue;
			}

			mPlugin.getLogger().fine("  " + label);

			newSoulParties.put(label, soulParty);

			soulPartyCount++;
		}

		mPlugin.getLogger().info("Soul pools:");
		int soulPoolCount = 0;
		for (JsonElement entry : soulPoolsArray) {
			JsonObject obj = entry.getAsJsonObject();

			SoulPoolEntry soulPool = SoulPoolEntry.fromJson(obj);
			String label = soulPool.getLabel();

			if (newSoulPools.get(label) != null) {
				mPlugin.getLogger().severe("Refused to load Library of Souls duplicate soul pool '" + label + "'");
				continue;
			}

			mPlugin.getLogger().fine("  " + label);

			newSoulPools.put(label, soulPool);

			soulPoolCount++;
		}

		final int finalSoulCount = soulCount;
		final int finalSoulPartyCount = soulPartyCount;
		final int finalSoulPoolCount = soulPoolCount;
		Bukkit.getScheduler().runTask(mPlugin, () -> {
			mSouls = newSouls;
			mSoulParties = newSoulParties;
			mSoulPools = newSoulPools;
			updateIndex();

			/* Reload the main plugin config / bestiary also after reloading the database */
			LibraryOfSouls.Config.load(mPlugin.getLogger(), mPlugin.getDataFolder());

			mPlugin.getLogger().info("Finished parsing souls library");
			mPlugin.getLogger().info("Loaded " + Integer.toString(finalSoulCount) + " mob souls");
			mPlugin.getLogger().info("Loaded " + Integer.toString(finalSoulPartyCount) + " mob soul parties");
			mPlugin.getLogger().info("Loaded " + Integer.toString(finalSoulPoolCount) + " mob soul pools");
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
		JsonArray soulArray = new JsonArray();
		for (SoulEntry soul : mSouls.values()) {
			soulArray.add(soul.toJson());
		}

		JsonArray soulPartyArray = new JsonArray();
		for (SoulPartyEntry soulParty : mSoulParties.values()) {
			soulPartyArray.add(soulParty.toJson());
		}

		JsonArray soulPoolArray = new JsonArray();
		for (SoulPoolEntry soulPool : mSoulPools.values()) {
			soulPoolArray.add(soulPool.toJson());
		}

		/* Mark the next change as expected so the watcher doesn't reload it */
		mIgnoreNextChange = true;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String soulsDatabasePath = mSoulsDatabasePath.toString();
		String soulPartiesDatabasePath = mSoulPartiesDatabasePath.toString();
		String soulPoolsDatabasePath = mSoulPoolsDatabasePath.toString();

		try {
			FileUtils.writeFile(soulsDatabasePath, gson.toJson(soulArray));
		} catch (Exception ex) {
			mPlugin.getLogger().severe("Failed to save souls database to '" + soulsDatabasePath + "': " + ex.getMessage());
		}

		try {
			FileUtils.writeFile(soulPartiesDatabasePath, gson.toJson(soulPartyArray));
		} catch (Exception ex) {
			mPlugin.getLogger().severe("Failed to save soul parties database to '" + soulPartiesDatabasePath + "': " + ex.getMessage());
		}

		try {
			FileUtils.writeFile(soulPoolsDatabasePath, gson.toJson(soulPoolArray));
		} catch (Exception ex) {
			mPlugin.getLogger().severe("Failed to save soul pools database to '" + soulPoolsDatabasePath + "': " + ex.getMessage());
		}
	}

	public static SoulsDatabase getInstance() {
		return INSTANCE;
	}

	public Set<String> listMobNames() {
		return new HashSet<>(mSouls.keySet());
	}

	public Set<String> listSoulPartyNames() {
		return new HashSet<>(mSoulParties.keySet());
	}

	public Set<String> listSoulPoolNames() {
		return new HashSet<>(mSoulPools.keySet());
	}

	public Set<String> listSoulGroupNames() {
		Set<String> result = new HashSet<String>(mSouls.keySet());
		result.addAll(mSoulParties.keySet());
		result.addAll(mSoulPools.keySet());
		return result;
	}

	public Set<String> listMobLocations() {
		return mLocsIndex.keySet();
	}

	public Set<String> listMobTypes() {
		return mTypesIndex.keySet();
	}
}
