package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.variables.BooleanVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.EffectsVariable;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryArea;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryEntryInterface;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryManager;
import com.playmonumenta.libraryofsouls.bestiary.BestiarySoulInventory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class SoulEntry implements Soul, BestiaryEntryInterface {
	public static final Gson GSON = new Gson();
	public static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();
	private final Set<String> mLocs;
	private final List<SoulHistoryEntry> mHistory;
	private List<Component> mLore;
	private @Nullable String mLorePrereqObjective;
	private int mLorePrereqMinScore = 0;
	private List<Component> mDescription;

	/* Create a SoulEntry object with existing history */
	public SoulEntry(List<SoulHistoryEntry> history, Set<String> locationNames, @Nullable List<Component> lore, @Nullable String lorePrereqObjective, int lorePrereqMinScore, List<Component> description) throws Exception {
		mHistory = history;

		if (locationNames == null) {
			mLocs = new HashSet<>();
		} else {
			mLocs = locationNames;
		}

		if (lore == null) {
			mLore = new ArrayList<>();
		} else {
			mLore = lore;
		}

		mLorePrereqObjective = lorePrereqObjective;
		mLorePrereqMinScore = lorePrereqMinScore;

		mDescription = Objects.requireNonNullElseGet(description, ArrayList::new);

		String refLabel = history.get(0).getLabel();

		for (SoulHistoryEntry entry : history) {
			if (!entry.getLabel().equals(refLabel)) {
				throw new Exception("Soul history has mismatching labels! '" + refLabel + "' != '" + entry.getLabel());
			}
		}
	}

	/* Create a new SoulEntry object from NBT */
	public SoulEntry(Player player, NBTTagCompound nbt) throws Exception {
		SoulHistoryEntry newHist = new SoulHistoryEntry(player, nbt);

		mLocs = new HashSet<>();
		mHistory = new ArrayList<>(1);
		mHistory.add(newHist);
		mLore = new ArrayList<>();
		mDescription = new ArrayList<>();
	}

	/* Update this SoulEntry so new soul is now current; preserve history */
	public void update(Player player, NBTTagCompound nbt) throws Exception {
		mHistory.add(0, new SoulHistoryEntry(player, nbt));
	}

	public void autoUpdate(Location loc) throws Exception {
		SoulHistoryEntry latestEntry = mHistory.get(0);
		if (latestEntry.requiresAutoUpdate()) {
			mHistory.add(0, latestEntry.getAutoUpdate(loc));
		}
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

	/*--------------------------------------------------------------------------------
	 * Soul Interface
	 */

	@Override
	public NBTTagCompound getNBT() {
		return mHistory.get(0).getNBT();
	}

	@Override
	public ItemStack getPlaceholder() {
		return mHistory.get(0).getPlaceholder();
	}

	@Override
	public ItemStack getBoS() {
		return mHistory.get(0).getBoS();
	}

	@Override
	public NamespacedKey getId() {
		return mHistory.get(0).getId();
	}

	@Override
	public Component getName() {
		return mHistory.get(0).getName();
	}

	@Override
	public Component getDisplayName() {
		return mHistory.get(0).getDisplayName();
	}

	@Override
	public boolean isBoss() {
		return mHistory.get(0).isBoss();
	}

	@Override
	public boolean isElite() {
		return mHistory.get(0).isElite();
	}

	@Override
	public Entity summon(Location loc) {
		return mHistory.get(0).summon(loc);
	}

	public void setLore(List<Component> lore, Player player) {
		mLore = lore;
		SoulsDatabase.getInstance().updateLore(this, player);
	}

	public List<Component> getLore() {
		return mLore;
	}

	public void setLorePrereq(String objective, int minScore, Player player) {
		mLorePrereqObjective = objective;
		mLorePrereqMinScore = minScore;
		SoulsDatabase.getInstance().updateLore(this, player);
	}

	public @Nullable String getLorePrereqObjective() {
		return mLorePrereqObjective;
	}

	public int getLorePrereqMinScore() {
		return mLorePrereqMinScore;
	}

	public boolean canSeeLore(Player player) {
		if (mLorePrereqObjective == null || mLorePrereqMinScore == 0) {
			return true;
		}
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(mLorePrereqObjective);
		if (objective == null) {
			return true;
		}
		return objective.getScore(player).getScore() >= mLorePrereqMinScore;
	}

	public void setDescription(List<Component> description, Player player) {
		mDescription = description;
		SoulsDatabase.getInstance().updateLore(this, player);
	}

	public List<Component> getDescription() {
		return mDescription;
	}

	/*
	 * Soul Interface
	 *--------------------------------------------------------------------------------*/

	/*--------------------------------------------------------------------------------
	 * BestiaryEntryInterface Interface
	 */

	private static final ItemStack NOT_FOUND_ITEM = new ItemStack(Material.PAPER);

	static {
		ItemMeta meta = NOT_FOUND_ITEM.getItemMeta();
		meta.displayName(Component.text("Mob not discovered!", NamedTextColor.DARK_RED, TextDecoration.ITALIC));
		NOT_FOUND_ITEM.setItemMeta(meta);
	}

	@Override
	public boolean canOpenBestiary(Player player) {
		return getInfoTier(player).allowsAccessTo(InfoTier.STATS);
	}

	@Override
	public ItemStack getBestiaryItem(Player player) {
		InfoTier info = getInfoTier(player);
		if (info.allowsAccessTo(InfoTier.MINIMAL)) {
			ItemStack item = new ItemStack(getPlaceholder());
			ItemMeta meta = item.getItemMeta();
			List<Component> lore = new ArrayList<>();

			lore.add(Component.text(BestiarySoulInventory.formatWell(getId().getKey()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("Kills: " + BestiaryManager.getKillsForMob(player, this), NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
			if (!mDescription.isEmpty()) {
				for (Component line : mDescription) {
					lore.add(line.color(NamedTextColor.DARK_GRAY));
				}
			}
			if (info.allowsAccessTo(InfoTier.STATS)) {
				lore.add(Component.text("Click for more info!", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
			}

			meta.lore(lore);

			// Hide weapon damage, book enchants, and potion effects:
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

			item.setItemMeta(meta);
			return item;
		}
		return NOT_FOUND_ITEM;
	}

	@Override
	public void openBestiary(Player player, @Nullable BestiaryArea parent, @Nullable List<BestiaryEntryInterface> peers, int peerIndex) {
		new BestiarySoulInventory(player, this, parent, !getInfoTier(player).allowsAccessTo(InfoTier.EVERYTHING), peers, peerIndex).openInventory(player, LibraryOfSouls.getInstance());
	}

	/*
	 * BestiaryEntryInterface Interface
	 *--------------------------------------------------------------------------------*/

	public enum InfoTier {
		EVERYTHING(3),
		STATS(2),
		MINIMAL(1),
		NOTHING(0);

		private final int mTier;

		InfoTier(int tier) {
			mTier = tier;
		}

		public boolean allowsAccessTo(InfoTier compareTo) {
			return mTier >= compareTo.mTier;
		}
	}

	public enum MobType {
		BOSS(1, 2),
		ELITE(5, 10),
		NORMAL(30, 60);

		private final int mFirstTier;
		private final int mSecondTier;

		MobType(int firstTier, int secondTier) {
			this.mFirstTier = firstTier;
			this.mSecondTier = secondTier;
		}

		public int getNeededKills(InfoTier tier) {
			if (tier == InfoTier.EVERYTHING) {
				return this.mSecondTier;
			} else if (tier == InfoTier.STATS) {
				return this.mSecondTier;
			} else if (tier == InfoTier.MINIMAL) {
				return this.mFirstTier;
			} else {
				return 1;
			}
		}
	}

	public InfoTier getInfoTier(Player player) {
		if (player.hasPermission("los.bestiary.viewall") || this.isInvulnerable()) {
			return InfoTier.EVERYTHING;
		}

		int kills = BestiaryManager.getKillsForMob(player, this);
		if (kills >= 1) {
			if (kills >= 60 || (isElite() && kills >= 10) || (isBoss() && kills >= 2)) {
				return InfoTier.EVERYTHING;
			} else if (kills >= 30 || (isElite() && kills >= 5) || isBoss()) {
				return InfoTier.STATS;
			} else {
				return InfoTier.MINIMAL;
			}
		}
		return InfoTier.NOTHING;
	}

	//Checks if the mob is invlunerable, for bestiary purposes
	public boolean isInvulnerable() {
		EntityNBT entityNBT = EntityNBT.fromEntityData(this.getNBT());
		EffectsVariable effectVar = new EffectsVariable("active_effects");
		BooleanVariable booVar = new BooleanVariable("Invulnerable");

		String ret = booVar.bind(entityNBT.getData()).get();
		boolean override = (ret != null && ret.equalsIgnoreCase("true"));

		ItemStack effectItem = ((EffectsVariable)effectVar.bind(entityNBT.getData())).getItem();
		if (effectItem != null && effectItem.hasItemMeta()) {
			PotionMeta potionMeta = (PotionMeta)effectItem.getItemMeta();
			for (PotionEffect effect : potionMeta.getCustomEffects()) {
				if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE) && effect.getAmplifier() >= 4) {
					override = true;
				}
			}
		}

		return override;
	}

	public MobType getMobType() {
		if (this.isBoss()) {
			return MobType.BOSS;
		} else if (this.isElite()) {
			return MobType.ELITE;
		} else {
			return MobType.NORMAL;
		}
	}

	public List<Soul> getHistory() {
		return new ArrayList<>(mHistory);
	}

	public Set<String> getLocationNames() {
		return mLocs;
	}

	public static SoulEntry fromJson(JsonObject obj, boolean loadHistory) throws Exception {
		Set<String> locs = new HashSet<>();
		JsonElement elem = obj.get("location_names");
		if (elem != null) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse location_names as JSON array");
			}

			for (JsonElement tagElement : array) {
				if (!tagElement.isJsonPrimitive()) {
					throw new Exception("location_names entry for '" + elem + "' is not a string!");
				}
				locs.add(tagElement.getAsString());
			}
		}

		List<Component> lore = new ArrayList<>();
		elem = obj.get("lore");
		if (elem != null && elem.isJsonArray()) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse lore as JSON array");
			}

			for (JsonElement loreElement : array) {
				if (!loreElement.isJsonPrimitive()) {
					throw new Exception("location_names entry for '" + elem + "' is not a string!");
				}
				Component comp = GSON_SERIALIZER.deserialize(loreElement.getAsString());
				lore.add(comp);
			}
		} else if (elem != null && elem.isJsonPrimitive()) {
			lore.add(Component.text(elem.getAsString()));
		}

		String lorePrereqObjective = null;
		elem = obj.get("lore_prereq_objective");
		if (elem != null && elem.isJsonPrimitive()) {
			lorePrereqObjective = elem.getAsString();
		}

		int lorePrereqMinScore = 0;
		elem = obj.get("lore_prereq_min_score");
		if (elem != null && elem.isJsonPrimitive()) {
			lorePrereqMinScore = elem.getAsInt();
		}

		List<Component> description = new ArrayList<>();
		elem = obj.get("description");
		if (elem != null && elem.isJsonArray()) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse description as JSON array");
			}

			for (JsonElement descriptionElement : array) {
				if (!descriptionElement.isJsonPrimitive()) {
					throw new Exception("description entry for '" + elem + "' is not a string!");
				}
				Component comp = GSON_SERIALIZER.deserialize(descriptionElement.getAsString());
				description.add(comp);
			}
		} else if (elem != null && elem.isJsonPrimitive()) {
			description.add(Component.text(elem.getAsString()));
		}

		List<SoulHistoryEntry> history = new ArrayList<>();
		elem = obj.get("history");
		if (elem != null) {
			JsonArray array = elem.getAsJsonArray();
			if (array == null) {
				throw new Exception("Failed to parse history as JSON array");
			}

			if (loadHistory) {
				for (JsonElement historyElement : array) {
					if (!historyElement.isJsonObject()) {
						throw new Exception("history entry for '" + elem + "' is not a string!");
					}

					history.add(SoulHistoryEntry.fromJson(historyElement.getAsJsonObject(), locs, lore, description));
				}
			} else {
				if (!array.isEmpty()) {
					JsonElement historyElement = array.get(0);
					if (!historyElement.isJsonObject()) {
						throw new Exception("history entry for '" + elem + "' is not a string!");
					}

					history.add(SoulHistoryEntry.fromJson(historyElement.getAsJsonObject(), locs, lore, description));
				}
			}
		}

		return new SoulEntry(history, locs, lore, lorePrereqObjective, lorePrereqMinScore, description);
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		JsonArray histArray = new JsonArray();
		for (SoulHistoryEntry hist : mHistory) {
			histArray.add(hist.toJson());
		}
		obj.add("history", histArray);

		JsonArray loreArray = new JsonArray();
		for (Component comp : mLore) {
			loreArray.add(GSON_SERIALIZER.serialize(comp));
		}
		obj.add("lore", loreArray);

		if (mLorePrereqObjective != null) {
			obj.add("lore_prereq_objective", new JsonPrimitive(mLorePrereqObjective));
		}

		obj.add("lore_prereq_min_score", new JsonPrimitive(mLorePrereqMinScore));

		JsonArray descriptionArray = new JsonArray();
		for (Component comp : mDescription) {
			descriptionArray.add(GSON_SERIALIZER.serialize(comp));
		}
		obj.add("description", descriptionArray);

		JsonArray locsArray = new JsonArray();
		for (String location : mLocs) {
			locsArray.add(location);
		}
		obj.add("location_names", locsArray);

		return obj;
	}
}
