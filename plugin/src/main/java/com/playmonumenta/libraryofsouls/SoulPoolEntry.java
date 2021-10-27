package com.playmonumenta.libraryofsouls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class SoulPoolEntry implements SoulGroup {
	private static Gson gson = null;

	private final List<SoulPoolHistoryEntry> mHistory;

	/* Create a SoulPoolEntry object with existing history */
	public SoulPoolEntry(List<SoulPoolHistoryEntry> history) throws Exception {
		mHistory = history;

		String refLabel = history.get(0).getLabel();

		for (SoulPoolHistoryEntry entry : history) {
			if (!entry.getLabel().equals(refLabel)) {
				throw new Exception("Soul Pool history has mismatching labels! '" + refLabel + "' != '" + entry.getLabel());
			}
		}
	}

	/* Create a new SoulPoolEntry object from NBT */
	public SoulPoolEntry(Player player, String label) {
		SoulPoolHistoryEntry newHist = new SoulPoolHistoryEntry(player, label);

		mHistory = new ArrayList<SoulPoolHistoryEntry>(1);
		mHistory.add(newHist);
	}

	/* Update this SoulPoolEntry so new soul is now current; preserve history */
	public void update(Player player, String entryLabel, int weight) throws WrapperCommandSyntaxException {
		mHistory.add(0, mHistory.get(0).changeWeight(player, entryLabel, weight));
	}

	public Map<String, Integer> getEntryWeights() {
		return mHistory.get(0).getEntryWeights();
	}

	/*--------------------------------------------------------------------------------
	 * Soul Group Interface
	 */

	@Override
	public String getLabel() {
		return mHistory.get(0).getLabel();
	}

	@Override
	public long getModifiedOn() {
		return mHistory.get(0).getModifiedOn();
	}

	@Override
	public String getModifiedBy() {
		return mHistory.get(0).getModifiedBy();
	}

	@Override
	public Set<Soul> getPossibleSouls() {
		return mHistory.get(0).getPossibleSouls();
	}

	@Override
	public Set<String> getPossibleSoulGroupLabels() {
		return mHistory.get(0).getPossibleSoulGroupLabels();
	}

	@Override
	public Map<SoulGroup, Integer> getRandomEntries(Random random) {
		return mHistory.get(0).getRandomEntries(random);
	}

	@Override
	public Map<SoulGroup, Double> getAverageEntries() {
		return mHistory.get(0).getAverageEntries();
	}

	@Override
	public Map<Soul, Integer> getRandomSouls(Random random) {
		return mHistory.get(0).getRandomSouls(random);
	}

	@Override
	public Map<Soul, Double> getAverageSouls() {
		return mHistory.get(0).getAverageSouls();
	}

	@Override
	public Double getWidth() {
		return mHistory.get(0).getWidth();
	}

	@Override
	public Double getHeight() {
		return mHistory.get(0).getHeight();
	}

	@Override
	public List<Entity> summonGroup(Random random, World world, BoundingBox spawnBb) {
		return mHistory.get(0).summonGroup(random, world, spawnBb);
	}

	/*
	 * Soul Group Interface
	 *--------------------------------------------------------------------------------*/

	public List<SoulGroup> getHistory() {
		return new ArrayList<SoulGroup>(mHistory);
	}

	public static SoulPoolEntry fromJson(JsonObject obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		List<SoulPoolHistoryEntry> history = new ArrayList<SoulPoolHistoryEntry>();
		JsonArray array = obj.getAsJsonArray("history");
		if (array == null) {
			throw new Exception("Failed to parse history as JSON array");
		}

		for (JsonElement historyElement : array) {
			if (!historyElement.isJsonObject()) {
				throw new Exception("history entry for '" + history.toString() + "' is not an object!");
			}

			history.add(SoulPoolHistoryEntry.fromJson(historyElement.getAsJsonObject()));
		}

		return new SoulPoolEntry(history);
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		JsonArray histArray = new JsonArray();
		for (SoulPoolHistoryEntry hist : mHistory) {
			histArray.add(hist.toJson());
		}
		obj.add("history", histArray);

		return obj;
	}
}
