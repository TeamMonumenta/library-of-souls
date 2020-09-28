package com.playmonumenta.libraryofsouls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SoulEntry implements Soul {
	private static Gson gson = null;

	private final Set<String> mLocs;
	private final List<SoulHistoryEntry> mHistory;

	/* Create a SoulEntry object with existing history */
	public SoulEntry(List<SoulHistoryEntry> history, Set<String> locationNames) throws Exception {
		mHistory = history;

		if (locationNames == null) {
			mLocs = new HashSet<String>();
		} else {
			mLocs = locationNames;
		}

		String refLabel = history.get(0).getLabel();
		String refName = history.get(0).getName();

		for (SoulHistoryEntry entry : history) {
			if (!entry.getLabel().equals(refLabel)) {
				throw new Exception("Soul history has mismatching names! '" + refName + "' != '" + entry.getName());
			}
		}
	}

	/* Create a new SoulEntry object from NBT */
	public SoulEntry(Player player, NBTTagCompound nbt) throws Exception {
		SoulHistoryEntry newHist = new SoulHistoryEntry(player, nbt);

		mLocs = new HashSet<String>();
		mHistory = new ArrayList<SoulHistoryEntry>(1);
		mHistory.add(newHist);
	}

	/* Update this SoulEntry so new soul is now current; preserve history */
	public void update(Player player, NBTTagCompound nbt) throws Exception {
		mHistory.add(0, new SoulHistoryEntry(player, nbt));
	}

	/*--------------------------------------------------------------------------------
	 * Soul Interface
	 */

	public NBTTagCompound getNBT() {
		return mHistory.get(0).getNBT();
	}

	public long getModifiedOn() {
		return mHistory.get(0).getModifiedOn();
	}

	public String getModifiedBy() {
		return mHistory.get(0).getModifiedBy();
	}

	public ItemStack getPlaceholder() {
		return mHistory.get(0).getPlaceholder();
	}

	public ItemStack getBoS() {
		return mHistory.get(0).getBoS();
	}

	public String getName() {
		return mHistory.get(0).getName();
	}

	public String getDisplayName() {
		return mHistory.get(0).getDisplayName();
	}

	public boolean isElite() {
		return mHistory.get(0).isElite();
	}

	public String getLabel() {
		return mHistory.get(0).getLabel();
	}

	public Entity summon(Location loc) {
		return mHistory.get(0).summon(loc);
	}

	/*
	 * Soul Interface
	 *--------------------------------------------------------------------------------*/

	public List<Soul> getHistory() {
		return new ArrayList<Soul>(mHistory);
	}

	public Set<String> getLocationNames() {
		return mLocs;
	}

	public static SoulEntry fromJson(JsonObject obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		JsonElement elem = obj.get("mojangson");

		Set<String> locs = new HashSet<String>();
		elem = obj.get("location_names");
		if (elem != null) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse location_names as JSON array");
			}

			Iterator<JsonElement> iter = array.iterator();
			while (iter.hasNext()) {
				JsonElement tagElement = iter.next();
				if (!tagElement.isJsonPrimitive()) {
					throw new Exception("location_names entry for '" + elem.toString() + "' is not a string!");
				}
				locs.add(tagElement.getAsString());
			}
		}

		List<SoulHistoryEntry> history = new ArrayList<SoulHistoryEntry>();
		elem = obj.get("history");
		if (elem != null) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse history as JSON array");
			}

			Iterator<JsonElement> iter = array.iterator();
			while (iter.hasNext()) {
				JsonElement historyElement = iter.next();
				if (!historyElement.isJsonObject()) {
					throw new Exception("history entry for '" + elem.toString() + "' is not a string!");
				}

				history.add(SoulHistoryEntry.fromJson(historyElement.getAsJsonObject(), locs));
			}
		}

		return new SoulEntry(history, locs);
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		JsonArray histArray = new JsonArray();
		for (SoulHistoryEntry hist : mHistory) {
			histArray.add(hist.toJson());
		}
		obj.add("history", histArray);

		JsonArray locsArray = new JsonArray();
		for (String location : mLocs) {
			locsArray.add(location);
		}
		obj.add("location_names", locsArray);

		return obj;
	}

}

