package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;
import com.playmonumenta.libraryofsouls.utils.Utils;
import com.playmonumenta.mixinapi.v1.DataFix;
import de.tr7zw.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class SoulsDatabase {
	private static final String SOULS_DATABASE_FILE = "souls_database.json";
	private static final String SOUL_PARTIES_DATABASE_FILE = "soul_parties_database.json";
	private static final String SOUL_POOLS_DATABASE_FILE = "soul_pools_database.json";

	private static @Nullable SoulsDatabase INSTANCE = null;

	private static final Comparator<String> COMPARATOR = String::compareToIgnoreCase;

	private final Plugin mPlugin;
	private final boolean mLoadHistory;
	private final Path mSoulsDatabasePath;
	private final Path mSoulPartiesDatabasePath;
	private final Path mSoulPoolsDatabasePath;
	private long mPreviousReloadMs = 0;
	private boolean mIgnoreNextChange = false;

	/* This is the primary database. One name, one SoulEntry per mob */
	private Map<String, SoulEntry> mSouls = new TreeMap<>(COMPARATOR);

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

	public SoulsDatabase(Plugin plugin, boolean loadHistory) throws Exception {
		mPlugin = plugin;
		mLoadHistory = loadHistory;
		mSoulsDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOULS_DATABASE_FILE);
		mSoulPartiesDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOUL_PARTIES_DATABASE_FILE);
		mSoulPoolsDatabasePath = Paths.get(mPlugin.getDataFolder().getPath(), SOUL_POOLS_DATABASE_FILE);

		/* Periodically check the file to see if it has changed */
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			try {
				mPlugin.getLogger().fine("Polling souls database file...");
				long lastModMs = Files.getLastModifiedTime(mSoulsDatabasePath).toMillis();
				lastModMs = Math.max(lastModMs, Files.getLastModifiedTime(mSoulPartiesDatabasePath).toMillis());
				lastModMs = Math.max(lastModMs, Files.getLastModifiedTime(mSoulPoolsDatabasePath).toMillis());
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
						sender.sendMessage(text("Failed to auto-update " + name + ": " + ex.getMessage(), RED));
					}
				} else {
					sender.sendMessage(text("Auto-update done.", NamedTextColor.GRAY));
					cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0L, 1L);
	}

	public @Nullable List<SoulEntry> getSoulsByLocation(@Nullable String location) {
		if (location == null) {
			return mNoLocMobs;
		} else {
			return mLocsIndex.get(location);
		}
	}

	public @Nullable List<SoulEntry> getSoulsByType(String id) {
		return mTypesIndex.get(id);
	}

	public List<SoulEntry> getSouls() {
		List<SoulEntry> souls = new ArrayList<>(mSouls.size());
		souls.addAll(mSouls.values());
		return souls;
	}

	public @Nullable SoulEntry getSoul(int index) {
		if (index >= mSouls.size()) {
			return null;
		}

		return (SoulEntry) mSouls.values().toArray()[index];
	}

	public @Nullable SoulEntry getSoul(String name) {
		return mSouls.get(name);
	}

	public List<SoulPartyEntry> getSoulParties() {
		List<SoulPartyEntry> soulParties = new ArrayList<>(mSoulParties.size());
		soulParties.addAll(mSoulParties.values());
		return soulParties;
	}

	public @Nullable SoulPartyEntry getSoulParty(int index) {
		if (index >= mSoulParties.size()) {
			return null;
		}

		return (SoulPartyEntry) mSoulParties.values().toArray()[index];
	}

	public @Nullable SoulPartyEntry getSoulParty(String label) {
		return mSoulParties.get(label);
	}

	public List<SoulPoolEntry> getSoulPools() {
		List<SoulPoolEntry> soulPools = new ArrayList<>(mSoulPools.size());
		soulPools.addAll(mSoulPools.values());
		return soulPools;
	}

	public @Nullable SoulPoolEntry getSoulPool(int index) {
		if (index >= mSoulPools.size()) {
			return null;
		}

		return (SoulPoolEntry) mSoulPools.values().toArray()[index];
	}

	public @Nullable SoulPoolEntry getSoulPool(String label) {
		return mSoulPools.get(label);
	}

	public @Nullable SoulGroup getSoulGroup(String label) {
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
			Component name = GsonComponentSerializer.gson().deserialize(nbt.getString("CustomName"));
			String label = Utils.getLabelFromName(name);

			if (mSouls.containsKey(label)) {
				sender.sendMessage(text("Mob '" + label + "' already exists!", RED));
				return;
			}

			soul = new SoulEntry(sender, nbt);
		} catch (Exception ex) {
			sender.sendMessage(text("Error parsing BoS: " + ex.getMessage(), RED));
			return;
		}

		mSouls.put(soul.getLabel(), soul);
		sender.sendMessage(text("Added " + soul.getLabel(), GREEN));
		updateIndex();
		save();
	}

	public void update(Player sender, BookOfSouls bos) {
		SoulEntry soul;

		try {
			NBTTagCompound nbt = bos.getEntityNBT().getData();
			Component name = GsonComponentSerializer.gson().deserialize(nbt.getString("CustomName"));
			String label = Utils.getLabelFromName(name);

			soul = mSouls.get(label);
			if (soul == null) {
				sender.sendMessage(text("Mob '" + label + "' does not exist!", RED));
				return;
			}

			if (!soul.getName().equals(name)) {
				sender.sendMessage(text("BoS name mismatches with existing name! Fix name capitalization and formatting.", RED));
				sender.sendMessage(text("LoS name: ", RED).append(soul.getName()));
				sender.sendMessage(text("BoS name: ", RED).append(name));
				return;
			}

			soul.update(sender, nbt);
		} catch (Exception ex) {
			sender.sendMessage(text("Error parsing BoS: " + ex.getMessage(), RED));
			return;
		}

		sender.sendMessage(text("Updated " + soul.getLabel(), GREEN));
		updateIndex();
		save();
	}

	// This function is only called in updateLore, where by definition the soul exists - also the bos doesnt change
	// internally, only on the outside but maybe that needs to happen?
	public void updateLore(SoulEntry soul, Player sender) {
		try {
			soul.update(sender, soul.getNBT());
		} catch (Exception ex) {
			sender.sendMessage("Exception when updating lore or description: " + ex + " for " + soul.getDisplayName());
		}
		save();
	}

	public void del(CommandSender sender, String name) {
		if (!mSouls.containsKey(name)) {
			sender.sendMessage(text("Mob '" + name + "' does not exist!", RED));
		} else {
			mSouls.remove(name);
			sender.sendMessage(text("Removed " + name, GREEN));
			updateIndex();
			save();
		}
	}

	public void addParty(Player player, String label) {
		SoulPartyEntry soulParty = new SoulPartyEntry(player, label);

		mSoulParties.put(soulParty.getLabel(), soulParty);
		player.sendMessage(text("Added " + soulParty.getLabel(), GREEN));
		save();
	}

	public void updateParty(Player player, String label, String entryLabel, int count) throws WrapperCommandSyntaxException {
		SoulPartyEntry soulParty = getSoulParty(label);
		if (soulParty == null) {
			throw CommandAPI.failWithString("Soul party '" + label + "' does not exist!");
		}

		SoulGroup soulGroup = getSoulGroup(entryLabel);
		if (soulGroup == null && count <= 0) {
			throw CommandAPI.failWithString("Soul group '" + entryLabel + "' does not exist!");
		}

		soulParty.update(player, entryLabel, count);
		player.sendMessage(text("Updated " + soulParty.getLabel(), GREEN));
		save();
	}

	public void delParty(CommandSender player, String label) {
		if (!mSoulParties.containsKey(label)) {
			player.sendMessage(text("Soul party '" + label + "' does not exist!", RED));
		} else {
			mSoulParties.remove(label);
			player.sendMessage(text("Removed " + label, GREEN));
			save();
		}
	}

	public void addPool(Player player, String label) {
		SoulPoolEntry soulPool = new SoulPoolEntry(player, label);

		mSoulPools.put(soulPool.getLabel(), soulPool);
		player.sendMessage(text("Added " + soulPool.getLabel(), GREEN));
		save();
	}

	public void updatePool(Player player, String label, String entryLabel, int weight) throws WrapperCommandSyntaxException {
		SoulPoolEntry soulPool = getSoulPool(label);
		if (soulPool == null) {
			throw CommandAPI.failWithString("Soul Pool '" + label + "' does not exist!");
		}

		SoulGroup soulGroup = getSoulGroup(entryLabel);
		if (soulGroup == null && weight <= 0) {
			throw CommandAPI.failWithString("Soul group '" + entryLabel + "' does not exist!");
		}

		soulPool.update(player, entryLabel, weight);
		player.sendMessage(text("Updated " + soulPool.getLabel(), GREEN));
		save();
	}

	public void delPool(CommandSender player, String label) {
		if (!mSoulPools.containsKey(label)) {
			player.sendMessage(text("Soul Pool '" + label + "' does not exist!", GREEN));
		} else {
			mSoulPools.remove(label);
			player.sendMessage(text("Removed " + label, GREEN));
			save();
		}
	}

	private JsonArray readSouls(Gson gson, String content) {
		JsonElement soulsArray = gson.fromJson(content, JsonElement.class);

		final JsonArray souls;
		int dataVersion = 3337;
		if (soulsArray instanceof JsonObject object) {
			if (object.has("data_version")) {
				dataVersion = object.get("data_version").getAsInt();
			}

			souls = object.getAsJsonArray("souls");
		} else {
			mPlugin.getLogger().info("Database is in legacy format, assuming data version: " + dataVersion);
			souls = (JsonArray) soulsArray;
		}

		try {
			Class.forName("com.playmonumenta.mixinapi.v1.DataFix");
			final var latestVersion = DataFix.getInstance().currentDataVersion();
			if (dataVersion < latestVersion) {
				mPlugin.getLogger().info("Performing datafix...");
				int entries = 0;
				for (JsonElement soul : souls) {
					for (JsonElement h : soul.getAsJsonObject().get("history").getAsJsonArray()) {
						entries++;
						final var history = h.getAsJsonObject();
						final var result = DataFix.getInstance().dataFix(
							new NBTContainer(history.get("mojangson").getAsString()),
							DataFix.Types.ENTITY, dataVersion, latestVersion
						);
						history.addProperty("mojangson", result.toString());
					}
				}
				mPlugin.getLogger().info("Datafixed " + entries + " entries");
			}
		} catch (ClassNotFoundException e) {
			mPlugin.getLogger().info("Monumenta mixin DFU api not found, skipping auto upgrade!");
		}

		return souls;
	}

	private String writeSouls(JsonArray soulArray, Gson gson) {
		final var object = new JsonObject();
		object.add("souls", soulArray);

		try {
			Class.forName("com.playmonumenta.mixinapi.v1.DataFix");
			final var dfuVersion = DataFix.getInstance().currentDataVersion();
			mPlugin.getLogger().info("Writing souls with DFU version: " + dfuVersion);
			object.addProperty("data_version", dfuVersion);
		} catch (ClassNotFoundException e) {
			mPlugin.getLogger().info("Monumenta mixin DFU api not found, skipping auto upgrade!");
		}

		return gson.toJson(object);
	}

	public void reloadAsync() throws Exception {
		mPlugin.getLogger().info("Reloading souls library...");
		Map<String, SoulEntry> newSouls = new TreeMap<>(COMPARATOR);
		Map<String, SoulPartyEntry> newSoulParties = new TreeMap<>(COMPARATOR);
		Map<String, SoulPoolEntry> newSoulPools = new TreeMap<>(COMPARATOR);

		String content = FileUtils.readFile(mSoulsDatabasePath.toString());
		if (content.isEmpty()) {
			throw new Exception("Failed to read souls database");
		}

		Gson gson = new Gson();
		JsonArray soulsArray = readSouls(gson, content);
		if (soulsArray == null) {
			throw new Exception("Failed to parse souls database as JSON array");
		}

		content = FileUtils.readFile(mSoulPartiesDatabasePath.toString());
		if (content.isEmpty()) {
			throw new Exception("Failed to read soul parties database");
		}

		JsonArray soulPartiesArray = gson.fromJson(content, JsonArray.class);
		if (soulPartiesArray == null) {
			throw new Exception("Failed to parse soul parties database as JSON array");
		}

		content = FileUtils.readFile(mSoulPoolsDatabasePath.toString());
		if (content.isEmpty()) {
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

			SoulEntry soul = SoulEntry.fromJson(obj, mLoadHistory);
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

			SoulPartyEntry soulParty = SoulPartyEntry.fromJson(obj, mLoadHistory);
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

			SoulPoolEntry soulPool = SoulPoolEntry.fromJson(obj, mLoadHistory);
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
			LibraryOfSouls.Config.load(mPlugin.getLogger(), mPlugin.getDataFolder(), true);

			mPlugin.getLogger().info("Finished parsing souls library");
			mPlugin.getLogger().info("Loaded " + finalSoulCount + " mob souls");
			mPlugin.getLogger().info("Loaded " + finalSoulPartyCount + " mob soul parties");
			mPlugin.getLogger().info("Loaded " + finalSoulPoolCount + " mob soul pools");
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
					List<SoulEntry> lst = mLocsIndex.computeIfAbsent(tag, k -> new ArrayList<>());
					lst.add(soul);
				}
			}

			/* Update type index */
			String id = soul.getId().getKey().toLowerCase(Locale.ROOT);
			List<SoulEntry> lst = mTypesIndex.computeIfAbsent(id, k -> new ArrayList<>());
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
			FileUtils.writeFile(soulsDatabasePath, writeSouls(soulArray, gson));
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
		if (INSTANCE == null) {
			throw new RuntimeException("SoulsDatabase not loaded");
		}
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
		Set<String> result = new HashSet<>(mSouls.keySet());
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
