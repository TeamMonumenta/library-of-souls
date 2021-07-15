package com.playmonumenta.libraryofsouls;

import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class SoulPoolHistoryEntry implements SoulGroup {
	private static Gson gson = null;

	private final String mLabel;
	private final long mModifiedOn;
	private final String mModifiedBy;
	private final Map<String, Integer> mEntryWeights;
	private final NavigableMap<Long, String> mNavigableMap;
	private final long mTotalWeight;

	/* Create a SoulPoolHistoryEntry object with existing history */
	public SoulPoolHistoryEntry(String label, long modifiedOn, String modifiedBy, Map<String, Integer> entryWeights) {
		if (!label.startsWith(LibraryOfSoulsAPI.SOUL_POOL_PREFIX)) {
			label = LibraryOfSoulsAPI.SOUL_POOL_PREFIX + label;
		}
		mLabel = label;
		mModifiedOn = modifiedOn;
		mModifiedBy = modifiedBy;
		mEntryWeights = entryWeights;

		long totalWeight = 0;
		NavigableMap<Long, String> navigableMap = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : mEntryWeights.entrySet()) {
			totalWeight += (long) entry.getValue();
			navigableMap.put(totalWeight, entry.getKey());
		}
		mNavigableMap = navigableMap;
		mTotalWeight = totalWeight;
	}

	/* Create a new SoulPoolHistoryEntry object with a label */
	public SoulPoolHistoryEntry(Player player, String label) {
		this(label, Instant.now().getEpochSecond(), player.getName(), new HashMap<String, Integer>());
	}

	/* Create a new SoulPoolHistoryEntry object with modified weights */
	public SoulPoolHistoryEntry changeWeight(Player player, String entryLabel, int weight) throws WrapperCommandSyntaxException {
		Map<String, Integer> newEntryWeights = new HashMap<>(mEntryWeights);
		if (weight <= 0) {
			if (!newEntryWeights.containsKey(entryLabel)) {
				CommandAPI.fail(getLabel() + " does not contain " + entryLabel);
			}
			newEntryWeights.remove(entryLabel);
		} else {
			SoulGroup entry = SoulsDatabase.getInstance().getSoulGroup(entryLabel);
			if (entry == null) {
				CommandAPI.fail(entryLabel + " does not exist.");
			}
			if (entry.getPossibleSoulGroupLabels().contains(getLabel())) {
				CommandAPI.fail(entryLabel + " contains " + getLabel());
			}
			newEntryWeights.put(entryLabel, weight);
		}
		return new SoulPoolHistoryEntry(mLabel, Instant.now().getEpochSecond(), player.getName(), newEntryWeights);
	}

	public Map<String, Integer> getEntryWeights() {
		return new HashMap<String, Integer>(mEntryWeights);
	}

	/*--------------------------------------------------------------------------------
	 * Soul Group Interface
	 */

	@Override
	public String getLabel() {
		return mLabel;
	}

	@Override
	public long getModifiedOn() {
		return mModifiedOn;
	}

	@Override
	public String getModifiedBy() {
		return mModifiedBy;
	}

	@Override
	public Set<Soul> getPossibleSouls() {
		Set<Soul> result = new HashSet<>();

		for (String groupLabel : mEntryWeights.keySet()) {
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(groupLabel);
			if (group != null) {
				result.addAll(group.getPossibleSouls());
			}
		}

		return result;
	}

	@Override
	public Set<String> getPossibleSoulGroupLabels() {
		Set<String> result = new HashSet<>();
		result.add(getLabel());
		for (Map.Entry<String, Integer> entry : mEntryWeights.entrySet()) {
			String groupLabel = entry.getKey();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(groupLabel);
			if (group != null) {
				result.addAll(group.getPossibleSoulGroupLabels());
			} else {
				result.add(groupLabel);
			}
		}
		return result;
	}

	@Override
	public Map<Soul, Integer> getRandomEntries(Random random) {
		if (mTotalWeight == 0) {
			return new HashMap<Soul, Integer>();
		}
		long randomValue = random.nextLong() % mTotalWeight;
		String selectedLabel = mNavigableMap.higherEntry(randomValue).getValue();
		SoulGroup selected = SoulsDatabase.getInstance().getSoulGroup(selectedLabel);
		if (selected != null) {
			return selected.getRandomEntries(random);
		} else {
			return new HashMap<Soul, Integer>();
		}
	}

	@Override
	public Map<Soul, Double> getAverageEntries() {
		Map<Soul, Double> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryWeights.entrySet()) {
			double percentChance = ((double) entry.getValue()) / mTotalWeight;
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (Map.Entry<Soul, Double> subEntry : group.getAverageEntries().entrySet()) {
					Soul soul = subEntry.getKey();
					double weightedAverage = percentChance * subEntry.getValue();
					if (result.containsKey(soul)) {
						weightedAverage += result.get(soul);
					}
					result.put(soul, weightedAverage);
				}
			}
		}

		return result;
	}

	/*
	 * Soul Group Interface
	 *--------------------------------------------------------------------------------*/

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		JsonObject entryWeightsObj = new JsonObject();
		for (Map.Entry<String, Integer> entry : mEntryWeights.entrySet()) {
			entryWeightsObj.addProperty(entry.getKey(), entry.getValue());
		}

		obj.addProperty("label", mLabel);
		obj.addProperty("modified_on", mModifiedOn);
		obj.addProperty("modified_by", mModifiedBy);
		obj.add("entry_weights", entryWeightsObj);

		return obj;
	}

	public static SoulPoolHistoryEntry fromJson(JsonObject obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		String label = obj.get("label").getAsString();
		long modifiedOn = obj.get("modified_on").getAsLong();
		String modifiedBy = obj.get("modified_by").getAsString();

		Map<String, Integer> entryWeights = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("entry_weights").entrySet()) {
			entryWeights.put(entry.getKey(), entry.getValue().getAsInt());
		}

		return new SoulPoolHistoryEntry(label, modifiedOn, modifiedBy, entryWeights);
	}
}