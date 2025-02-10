package com.playmonumenta.libraryofsouls;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class SoulPartyEntry implements SoulGroup {
	private final List<SoulPartyHistoryEntry> mHistory;

	/* Create a SoulPartyEntry object with existing history */
	public SoulPartyEntry(List<SoulPartyHistoryEntry> history) throws Exception {
		mHistory = history;

		String refLabel = history.get(0).getLabel();

		for (SoulPartyHistoryEntry entry : history) {
			if (!entry.getLabel().equalsIgnoreCase(refLabel)) {
				throw new Exception("Soul party history has mismatching labels! '" + refLabel + "' != '" + entry.getLabel());
			}
		}
	}

	/* Create a new SoulPartyEntry object from NBT */
	public SoulPartyEntry(Player player, String label) {
		SoulPartyHistoryEntry newHist = new SoulPartyHistoryEntry(player, label);

		mHistory = new ArrayList<>(1);
		mHistory.add(newHist);
	}

	/* Update this SoulPartyEntry so new soul is now current; preserve history */
	public void update(Player player, String entryLabel, int count) throws WrapperCommandSyntaxException {
		mHistory.add(0, mHistory.get(0).changeCount(player, entryLabel, count));
	}

	public Map<String, Integer> getEntryCounts() {
		return mHistory.get(0).getEntryCounts();
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
	public @Nullable Double getWidth() {
		return mHistory.get(0).getWidth();
	}

	@Override
	public @Nullable Double getHeight() {
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
		return new ArrayList<>(mHistory);
	}

	public static SoulPartyEntry fromJson(JsonObject obj, boolean loadHistory) throws Exception {
		List<SoulPartyHistoryEntry> history = new ArrayList<>();
		JsonArray array = obj.getAsJsonArray("history");
		if (array == null) {
			throw new Exception("Failed to parse history as JSON array");
		}

		if (loadHistory) {
			for (JsonElement historyElement : array) {
				if (!historyElement.isJsonObject()) {
					throw new Exception("history entry for '" + history + "' is not an object!");
				}

				history.add(SoulPartyHistoryEntry.fromJson(historyElement.getAsJsonObject()));
			}
		} else {
			if (!array.isEmpty()) {
				JsonElement historyElement = array.get(0);
				if (!historyElement.isJsonObject()) {
					throw new Exception("history entry for '" + history + "' is not an object!");
				}

				history.add(SoulPartyHistoryEntry.fromJson(historyElement.getAsJsonObject()));
			}
		}

		return new SoulPartyEntry(history);
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		JsonArray histArray = new JsonArray();
		for (SoulPartyHistoryEntry hist : mHistory) {
			histArray.add(hist.toJson());
		}
		obj.add("history", histArray);

		return obj;
	}
}
