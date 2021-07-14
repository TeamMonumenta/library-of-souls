package com.playmonumenta.libraryofsouls;

import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class SoulPartyHistoryEntry implements SoulGroup {
	private static Gson gson = null;

	private final String mLabel;
	private final long mModifiedOn;
	private final String mModifiedBy;
	private final Map<String, Integer> mEntryCounts;

	/* Create a SoulPartyHistoryEntry object with existing history */
	public SoulPartyHistoryEntry(String label, long modifiedOn, String modifiedBy, Map<String, Integer> entryCounts) {
		if (!label.startsWith(LibraryOfSoulsAPI.SOUL_PARTY_PREFIX)) {
			label = LibraryOfSoulsAPI.SOUL_PARTY_PREFIX + label;
		}
		mLabel = label;
		mModifiedOn = modifiedOn;
		mModifiedBy = modifiedBy;
		mEntryCounts = entryCounts;
	}

	/* Create a new SoulPartyHistoryEntry object with a label */
	public SoulPartyHistoryEntry(Player player, String label) {
		this(label, Instant.now().getEpochSecond(), player.getName(), new HashMap<String, Integer>());
	}

	/* Create a new SoulPartyHistoryEntry object with modified counts */
	public SoulPartyHistoryEntry changeCount(Player player, String entryLabel, int count) throws WrapperCommandSyntaxException {
		Map<String, Integer> newEntryCounts = new HashMap<>(mEntryCounts);
		if (count <= 0) {
			if (!newEntryCounts.containsKey(entryLabel)) {
				CommandAPI.fail(getLabel() + " does not contain " + entryLabel);
			}
			newEntryCounts.remove(entryLabel);
		} else {
			SoulGroup entry = SoulsDatabase.getInstance().getSoulGroup(entryLabel);
			if (entry == null) {
				CommandAPI.fail(entryLabel + " does not exist.");
			}
			if (entry.getPossibleSoulGroupLabels().contains(getLabel())) {
				CommandAPI.fail(entryLabel + " contains " + getLabel());
			}
			newEntryCounts.put(entryLabel, count);
		}
		return new SoulPartyHistoryEntry(mLabel, Instant.now().getEpochSecond(), player.getName(), newEntryCounts);
	}

	public Map<String, Integer> getEntryCounts() {
		return new HashMap<String, Integer>(mEntryCounts);
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

		for (String groupLabel : mEntryCounts.keySet()) {
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
		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
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
		Map<Soul, Integer> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (Map.Entry<Soul, Integer> subEntry : group.getRandomEntries(random).entrySet()) {
					Soul soul = subEntry.getKey();
					Integer count = result.get(soul);
					if (count == null) {
						count = subEntry.getValue();
					} else {
						count += subEntry.getValue();
					}
					result.put(soul, count);
				}
			}
		}

		return result;
	}

	@Override
	public Map<Soul, Double> getAverageEntries() {
		Map<Soul, Double> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			double entryCount = (double) entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (Map.Entry<Soul, Double> subEntry : group.getAverageEntries().entrySet()) {
					Soul soul = subEntry.getKey();
					double weightedAverage = entryCount * subEntry.getValue();
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

		JsonObject entryCountsObj = new JsonObject();
		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			entryCountsObj.addProperty(entry.getKey(), entry.getValue());
		}

		obj.addProperty("label", mLabel);
		obj.addProperty("modified_on", mModifiedOn);
		obj.addProperty("modified_by", mModifiedBy);
		obj.add("entry_counts", entryCountsObj);

		return obj;
	}

	public static SoulPartyHistoryEntry fromJson(JsonObject obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		String label = obj.get("label").getAsString();
		long modifiedOn = obj.get("modified_on").getAsLong();
		String modifiedBy = obj.get("modified_by").getAsString();

		Map<String, Integer> entryCounts = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("entry_counts").entrySet()) {
			entryCounts.put(entry.getKey(), entry.getValue().getAsInt());
		}

		return new SoulPartyHistoryEntry(label, modifiedOn, modifiedBy, entryCounts);
	}
}
