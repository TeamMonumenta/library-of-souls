package com.playmonumenta.libraryofsouls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class SoulPartyHistoryEntry implements SoulGroup {
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
		this(label, Instant.now().getEpochSecond(), player.getName(), new HashMap<>());
	}

	/* Create a new SoulPartyHistoryEntry object with modified counts */
	public SoulPartyHistoryEntry changeCount(Player player, String entryLabel, int count) throws WrapperCommandSyntaxException {
		Map<String, Integer> newEntryCounts = new HashMap<>(mEntryCounts);
		if (count <= 0) {
			if (!newEntryCounts.containsKey(entryLabel)) {
				throw CommandAPI.failWithString(getLabel() + " does not contain " + entryLabel);
			}
			newEntryCounts.remove(entryLabel);
		} else {
			SoulGroup entry = SoulsDatabase.getInstance().getSoulGroup(entryLabel);
			if (entry == null) {
				throw CommandAPI.failWithString(entryLabel + " does not exist.");
			}
			if (entry.getPossibleSoulGroupLabels().contains(getLabel())) {
				throw CommandAPI.failWithString(entryLabel + " contains " + getLabel());
			}
			newEntryCounts.put(entryLabel, count);
		}
		return new SoulPartyHistoryEntry(mLabel, Instant.now().getEpochSecond(), player.getName(), newEntryCounts);
	}

	public Map<String, Integer> getEntryCounts() {
		return new HashMap<>(mEntryCounts);
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
	public Map<SoulGroup, Integer> getRandomEntries(Random random) {
		Map<SoulGroup, Integer> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			int entryCount = entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				result.put(group, entryCount);
			}
		}

		return result;
	}

	@Override
	public Map<SoulGroup, Double> getAverageEntries() {
		Map<SoulGroup, Double> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			double entryCount = (double) entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				result.put(group, entryCount);
			}
		}

		return result;
	}

	@Override
	public Map<Soul, Integer> getRandomSouls(Random random) {
		Map<Soul, Integer> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			int entryCount = entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (int entryIndex = 0; entryIndex < entryCount; ++entryIndex) {
					for (Map.Entry<Soul, Integer> subEntry : group.getRandomSouls(random).entrySet()) {
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
		}

		return result;
	}

	@Override
	public Map<Soul, Double> getAverageSouls() {
		Map<Soul, Double> result = new HashMap<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			double entryCount = (double) entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (Map.Entry<Soul, Double> subEntry : group.getAverageSouls().entrySet()) {
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

	@Override
	public @Nullable Double getWidth() {
		Double result = null;
		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				Double groupWidth = group.getWidth();
				if (result == null) {
					result = groupWidth;
				} else if (groupWidth != null) {
					result = Math.max(result, groupWidth);
				}
			}
		}
		return result;
	}

	@Override
	public @Nullable Double getHeight() {
		Double result = null;
		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				Double groupHeight = group.getHeight();
				if (result == null) {
					result = groupHeight;
				} else if (groupHeight != null) {
					result = Math.max(result, groupHeight);
				}
			}
		}
		return result;
	}

	@Override
	public List<Entity> summonGroup(Random random, World world, BoundingBox spawnBb) {
		List<Entity> result = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : mEntryCounts.entrySet()) {
			int entryCount = entry.getValue();
			SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(entry.getKey());
			if (group != null) {
				for (int i = 0; i < entryCount; i++) {
					result.addAll(group.summonGroup(random, world, spawnBb));
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
