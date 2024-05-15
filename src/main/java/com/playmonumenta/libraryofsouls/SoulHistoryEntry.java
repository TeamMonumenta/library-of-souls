package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTTagList;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.ItemStackNBTWrapper;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ListVariable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.Utils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class SoulHistoryEntry implements Soul {
	private static class HitboxSize {
		private double mWidth;
		private double mHeight;

		public HitboxSize(Location origin, NBTTagCompound nbt) {
			Entity entity = EntityNBT.fromEntityData(nbt).spawn(origin);
			BoundingBox bb = getRecursiveBoundingBox(entity);

			// TODO get width and height of bounding box relative to origin (ignore height below origin, because boats are whack)
			mWidth = Math.max(Math.max(bb.getMaxX() - origin.getX(),
			                           bb.getMaxZ() - origin.getZ()),
			                  Math.max(origin.getX() - bb.getMinX(),
			                           origin.getZ() - bb.getMinZ()));
			mHeight = bb.getMaxY() - origin.getY();
		}

		private BoundingBox getRecursiveBoundingBox(Entity entity) {
			BoundingBox bb = entity.getBoundingBox();
			for (Entity passenger : entity.getPassengers()) {
				bb.union(getRecursiveBoundingBox(passenger));
			}
			entity.remove();
			return bb;
		}

		public double width() {
			return mWidth;
		}

		public double height() {
			return mHeight;
		}
	}

	private final NBTTagCompound mNBT;
	private final long mModifiedOn;
	private final String mModifiedBy;
	private final Component mName;
	private final String mLabel;
	private final Set<String> mLocs;
	private final NamespacedKey mId;
	private final List<Component> mLore;
	private final List<Component> mDescription;
	private final @Nullable Double mWidth;
	private final @Nullable Double mHeight;
	private @Nullable ItemStack mPlaceholder = null;
	private @Nullable ItemStack mBoS = null;

	/* Create a SoulHistoryEntry object with existing history */
	public SoulHistoryEntry(NBTTagCompound nbt, long modifiedOn, String modifiedBy, Set<String> locations, List<Component> lore, List<Component> description, @Nullable Double width, @Nullable Double height) throws Exception {
		mNBT = nbt;
		mModifiedOn = modifiedOn;
		mModifiedBy = modifiedBy;
		mLocs = locations;
		mId = EntityNBT.fromEntityData(mNBT).getEntityType().getKey();
		mLore = lore;
		mDescription = description;
		mWidth = width;
		mHeight = height;

		mName = GsonComponentSerializer.gson().deserialize(nbt.getString("CustomName"));
		mLabel = Utils.getLabelFromName(Utils.plainText(mName));
		if (mLabel == null || mLabel.isEmpty()) {
			throw new Exception("Refused to load Library of Souls mob with no name!");
		}
	}

	/* Create a new SoulHistoryEntry object from NBT */
	public SoulHistoryEntry(Player player, NBTTagCompound nbt) throws Exception {
		Location loc = player.getLocation().clone();
		loc.setY(loc.getWorld().getMaxHeight());
		HitboxSize hitboxSize = new HitboxSize(loc, nbt);

		mNBT = nbt;
		mModifiedOn = Instant.now().getEpochSecond();
		mModifiedBy = player.getName();
		mLocs = new HashSet<String>();
		mId = EntityNBT.fromEntityData(mNBT).getEntityType().getKey();
		mLore = new ArrayList<>();
		mDescription = new ArrayList<>();
		mWidth = hitboxSize.width();
		mHeight = hitboxSize.height();

		mName = GsonComponentSerializer.gson().deserialize(nbt.getString("CustomName"));
		mLabel = Utils.getLabelFromName(Utils.plainText(mName));
		if (mLabel == null || mLabel.isEmpty()) {
			throw new Exception("Refused to load Library of Souls mob with no name!");
		}
	}

	public boolean requiresAutoUpdate() {
		return (mWidth == null) || (mHeight == null);
	}

	public SoulHistoryEntry getAutoUpdate(Location loc) throws Exception {
		HitboxSize hitboxSize = new HitboxSize(loc, mNBT);
		return new SoulHistoryEntry(mNBT,
			Instant.now().getEpochSecond(),
			"AutoUpdate",
			mLocs,
			mLore,
			mDescription,
			hitboxSize.width(),
			hitboxSize.height());
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
		result.add(this);
		return result;
	}

	@Override
	public Set<String> getPossibleSoulGroupLabels() {
		Set<String> result = new HashSet<>();
		result.add(getLabel());
		return result;
	}

	@Override
	public Map<SoulGroup, Integer> getRandomEntries(Random random) {
		Map<SoulGroup, Integer> result = new HashMap<>();
		result.put(this, 1);
		return result;
	}

	@Override
	public Map<SoulGroup, Double> getAverageEntries() {
		Map<SoulGroup, Double> result = new HashMap<>();
		result.put(this, 1.0);
		return result;
	}

	@Override
	public Map<Soul, Integer> getRandomSouls(Random random) {
		Map<Soul, Integer> result = new HashMap<>();
		result.put(this, 1);
		return result;
	}

	@Override
	public Map<Soul, Double> getAverageSouls() {
		Map<Soul, Double> result = new HashMap<>();
		result.put(this, 1.0);
		return result;
	}

	@Override
	public @Nullable Double getWidth() {
		return mWidth;
	}

	@Override
	public @Nullable Double getHeight() {
		return mHeight;
	}

	@Override
	public List<Entity> summonGroup(Random random, World world, BoundingBox spawnBb) {
		List<Entity> result = new ArrayList<>();
		if (mWidth == null || mHeight == null) {
			return result;
		}
		double x = spawnBb.getMinX() + random.nextDouble() * (spawnBb.getMaxX() - spawnBb.getMinX());
		double y = spawnBb.getMinY() + random.nextDouble() * (spawnBb.getMaxY() - spawnBb.getMinY());
		double z = spawnBb.getMinZ() + random.nextDouble() * (spawnBb.getMaxZ() - spawnBb.getMinZ());
		Location loc = new Location(world, x, y, z);
		if (!Utils.insideBlocks(loc, mWidth, mHeight)) {
			result.add(summon(loc));
		}
		return result;
	}

	/*
	 * Soul Group Interface
	 *--------------------------------------------------------------------------------*/

	/*--------------------------------------------------------------------------------
	 * Soul Interface
	 */

	@Override
	public NBTTagCompound getNBT() {
		return mNBT;
	}

	@Override
	public ItemStack getPlaceholder() {
		if (mPlaceholder == null) {
			regenerateItems();
		}
		if (mPlaceholder == null) {
			throw new RuntimeException("Placeholder is null despite regeneration");
		}
		return mPlaceholder;
	}

	@Override
	public ItemStack getBoS() {
		if (mBoS == null) {
			regenerateItems();
		}
		if (mBoS == null) {
			throw new RuntimeException("BoS is null despite regeneration");
		}
		return mBoS;
	}

	@Override
	public NamespacedKey getId() {
		return mId;
	}

	@Override
	public Component getName() {
		return mName;
	}

	@Override
	public Component getDisplayName() {
		return Component.text(Utils.plainText(mName), isElite() ? NamedTextColor.GOLD : isBoss() ? NamedTextColor.RED : NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public boolean isBoss() {
		boolean isBoss = false;
		NBTTagList tags = mNBT.getList("Tags");
		if (tags != null && tags.size() > 0) {
			for (Object obj : tags.getAsArray()) {
				if (obj.equals("Boss")) {
					isBoss = true;
				}
			}
		}
		return isBoss;
	}

	@Override
	public boolean isElite() {
		boolean isElite = false;
		NBTTagList tags = mNBT.getList("Tags");
		if (tags != null && tags.size() > 0) {
			for (Object obj : tags.getAsArray()) {
				if (obj.equals("Elite")) {
					isElite = true;
				}
			}
		}
		return isElite;
	}

	@Override
	public Entity summon(Location loc) {
		return EntityNBT.fromEntityData(mNBT).spawn(loc);
	}

	/*
	 * Soul Interface
	 *--------------------------------------------------------------------------------*/

	private List<String> stringifyWrapList(String prefix, int maxLen, Object[] elements) {
		List<String> ret = new ArrayList<String>();

		String cur = "" + prefix;
		boolean first = true;
		for (Object element : elements) {
			String entry = (String)element;

			String temp;
			if (first) {
				temp = cur + Utils.hashColor(entry);
			} else {
				temp = cur + " " + Utils.hashColor(entry);
			}
			first = false;

			if (ChatColor.stripColor(temp).length() <= maxLen) {
				cur = temp;
			} else {
				ret.add(cur);
				cur = prefix + Utils.hashColor(entry);
			}
		}

		ret.add(cur);

		return ret;
	}

	private void regenerateItems() {
		EntityNBT entityNBT = EntityNBT.fromEntityData(mNBT);

		try {
			mBoS = new BookOfSouls(entityNBT).getBook();
		} catch (Exception ex) {
			Logger logger = LibraryOfSouls.getInstance().getLogger();
			logger.warning("Library of souls entry for '" + mName + "' failed to load: " + ex.getMessage());
			ex.printStackTrace();

			mPlaceholder = new ItemStack(Material.BARRIER);
			mPlaceholder = mPlaceholder.ensureServerConversions();
			ItemStackNBTWrapper placeholderWrap = new ItemStackNBTWrapper(mPlaceholder);
			placeholderWrap.getVariable("Name").set("FAILED TO LOAD: " + getDisplayName(), null);
			placeholderWrap.save();

			mBoS = mPlaceholder.clone();
			return;
		}

		Material material = switch (entityNBT.getEntityType()) {
			case ALLAY -> Material.AMETHYST_SHARD;
			case ARMOR_STAND -> Material.ARMOR_STAND;
			case AXOLOTL -> Material.AXOLOTL_BUCKET;
			case BLAZE -> Material.BLAZE_POWDER;
			case BEE -> Material.HONEYCOMB;
			case CAT -> Material.STRING;
			case CAMEL -> Material.SANDSTONE;
			case CAVE_SPIDER -> Material.FERMENTED_SPIDER_EYE;
			case CHICKEN -> Material.CHICKEN;
			case COD -> Material.COD;
			case COW -> Material.BEEF;
			case CREEPER -> Material.CREEPER_HEAD;
			case DOLPHIN -> Material.COD;
			case DROWNED -> Material.TRIDENT;
			case ELDER_GUARDIAN -> Material.SPONGE;
			case ENDERMAN -> Material.ENDER_PEARL;
			case ENDERMITE -> Material.ENDER_EYE;
			case ENDER_CRYSTAL -> Material.END_CRYSTAL;
			case ENDER_DRAGON -> Material.DRAGON_HEAD;
			case EVOKER -> Material.TOTEM_OF_UNDYING;
			case EVOKER_FANGS -> Material.DEAD_FIRE_CORAL_FAN;
			case FALLING_BLOCK -> Material.SAND;
			case FOX -> Material.SWEET_BERRIES;
			case FROG -> Material.LILY_PAD;
			case GHAST -> Material.GHAST_TEAR;
			case GIANT -> Material.ANCIENT_DEBRIS;
			case GLOW_SQUID -> Material.GLOW_INK_SAC;
			case GUARDIAN -> Material.PRISMARINE_SHARD;
			case HOGLIN -> Material.WARPED_FUNGUS;
			case ZOGLIN -> Material.CRIMSON_FUNGUS;
			case HORSE -> Material.SADDLE;
			case HUSK -> Material.ROTTEN_FLESH;
			case ILLUSIONER -> Material.BOW;
			case IRON_GOLEM -> Material.IRON_BLOCK;
			case MAGMA_CUBE -> Material.MAGMA_CREAM;
			case MUSHROOM_COW -> Material.RED_MUSHROOM;
			case OCELOT -> Material.COOKED_CHICKEN;
			case PANDA -> Material.BAMBOO;
			case PARROT -> Material.FEATHER;
			case PILLAGER -> Material.CROSSBOW;
			case PIG -> Material.PORKCHOP;
			case PHANTOM -> Material.PHANTOM_MEMBRANE;
			case POLAR_BEAR -> Material.SNOW;
			case SPLASH_POTION -> Material.GLASS_BOTTLE;
			case ZOMBIFIED_PIGLIN -> Material.GOLD_NUGGET;
			case PIGLIN -> Material.GOLDEN_BOOTS;
			case PIGLIN_BRUTE -> Material.GOLDEN_AXE;
			case PUFFERFISH -> Material.PUFFERFISH;
			case RABBIT -> Material.RABBIT_FOOT;
			case RAVAGER -> Material.SHIELD;
			case SALMON -> Material.SALMON;
			case SHEEP -> Material.WHITE_WOOL;
			case SHULKER -> Material.SHULKER_BOX;
			case SILVERFISH -> Material.MOSSY_STONE_BRICKS;
			case SKELETON -> Material.SKELETON_SKULL;
			case SKELETON_HORSE -> Material.IRON_HORSE_ARMOR;
			case SLIME -> Material.SLIME_BALL;
			case SNIFFER -> Material.TORCHFLOWER;
			case SNOWMAN -> Material.CARVED_PUMPKIN;
			case SPIDER -> Material.SPIDER_EYE;
			case STRAY -> Material.BOW;
			case STRIDER -> Material.WARPED_FUNGUS_ON_A_STICK;
			case SQUID -> Material.INK_SAC;
			case TADPOLE -> Material.FROGSPAWN;
			case TROPICAL_FISH -> Material.TROPICAL_FISH;
			case TURTLE -> Material.TURTLE_HELMET;
			case VEX -> Material.IRON_SWORD;
			case VINDICATOR -> Material.STONE_AXE;
			case VILLAGER -> Material.EMERALD;
			case WARDEN -> Material.SCULK_SHRIEKER;
			case WITCH -> Material.POISONOUS_POTATO;
			case WITHER -> Material.NETHER_STAR;
			case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
			case WOLF -> Material.BONE;
			case ZOMBIE -> Material.ZOMBIE_HEAD;
			case ZOMBIE_VILLAGER -> Material.BELL;
			case ZOMBIE_HORSE -> Material.LEATHER;
			default -> null;
		};

		if (material == null) {
			mPlaceholder = mBoS.clone();
		} else {
			mPlaceholder = new ItemStack(material);
		}

		mPlaceholder = mPlaceholder.ensureServerConversions();
		ItemMeta placeholderMeta = mPlaceholder.getItemMeta();
		placeholderMeta.displayName(Component.text(""));
		placeholderMeta.lore(List.of());
		mPlaceholder.setItemMeta(placeholderMeta);
		mPlaceholder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

		mBoS = mBoS.ensureServerConversions();
		ItemMeta bosMeta = mBoS.getItemMeta();
		bosMeta.displayName(Component.text(""));
		bosMeta.lore(List.of());
		mBoS.setItemMeta(bosMeta);

		ItemStackNBTWrapper placeholderWrap = new ItemStackNBTWrapper(mPlaceholder);
		ItemStackNBTWrapper bosWrap = new ItemStackNBTWrapper(mBoS);

		/* Set the item's display name (recolored, does not exactly match actual mob name) */
		String serializedDisplayName = GsonComponentSerializer.gson().serialize(getDisplayName());
		placeholderWrap.getVariable("Name").set(serializedDisplayName, null);
		bosWrap.getVariable("Name").set(serializedDisplayName, null);

		/* Set hide flags to hide the BoS author info */
		placeholderWrap.getVariable("HideFlags").set("32", null);
		bosWrap.getVariable("HideFlags").set("32", null);

		String idStr = ChatColor.WHITE + "Type: ";
		if (mNBT.getString("id").startsWith("minecraft:")) {
			idStr += mNBT.getString("id").substring(10);
		} else {
			idStr += mNBT.getString("id");
		}
		((ListVariable)placeholderWrap.getVariable("Lore")).add(idStr, null);
		((ListVariable)bosWrap.getVariable("Lore")).add(idStr, null);

		if (mNBT.hasKey("Health")) {
			String healthStr = ChatColor.WHITE + "Health: " + Double.toString(mNBT.getDouble("Health"));
			((ListVariable)placeholderWrap.getVariable("Lore")).add(healthStr, null);
			((ListVariable)bosWrap.getVariable("Lore")).add(healthStr, null);
		}

		NBTTagList tags = mNBT.getList("Tags");
		if (tags != null && tags.size() > 0) {
			((ListVariable)placeholderWrap.getVariable("Lore")).add(ChatColor.WHITE + "Tags:", null);
			((ListVariable)bosWrap.getVariable("Lore")).add(ChatColor.WHITE + "Tags:", null);

			int index = 0;
			for (String str : stringifyWrapList("  ", 50, tags.getAsArray())) {
				index = 0;
				while (str.length() - index >= 40) {
					((ListVariable)placeholderWrap.getVariable("Lore")).add(str.substring(index, index + 40), null);
					((ListVariable)bosWrap.getVariable("Lore")).add(str.substring(index, index + 40), null);
					index += 40;
				}

				((ListVariable)placeholderWrap.getVariable("Lore")).add(str.substring(index), null);
				((ListVariable)bosWrap.getVariable("Lore")).add(str.substring(index), null);
			}
		}

		if (mLocs != null && mLocs.size() > 0) {
			((ListVariable)placeholderWrap.getVariable("Lore")).add(ChatColor.WHITE + "Locations:", null);
			((ListVariable)bosWrap.getVariable("Lore")).add(ChatColor.WHITE + "Locations:", null);

			for (String str : stringifyWrapList("  ", 45, mLocs.toArray())) {
				((ListVariable)placeholderWrap.getVariable("Lore")).add(str, null);
				((ListVariable)bosWrap.getVariable("Lore")).add(str, null);
			}
		}

		if (mLore != null && !mLore.isEmpty()) {
			((ListVariable)placeholderWrap.getVariable("Lore")).add(ChatColor.WHITE + "Lore:", null);
			((ListVariable)bosWrap.getVariable("Lore")).add(ChatColor.WHITE + "Lore:", null);
			//Rather than a giant block of text, two words suffice.
			((ListVariable)placeholderWrap.getVariable("Lore")).add("It exists.", null);
		}

		if (mDescription != null && !mDescription.isEmpty()) {
			((ListVariable)placeholderWrap.getVariable("Lore")).add(ChatColor.WHITE + "Description:", null);
			((ListVariable)bosWrap.getVariable("Lore")).add(ChatColor.WHITE + "Description:", null);
			// Rather than a giant block of text, two words suffice.
			((ListVariable)placeholderWrap.getVariable("Lore")).add("It exists.", null);
		}

		/* If the item has been modified, list when */
		if (mModifiedBy != null && !mModifiedBy.isEmpty()) {
			/* Relative time on the placeholder item */
			((ListVariable)placeholderWrap.getVariable("Lore")).add(ChatColor.AQUA + "Modified " + getTimeDeltaStr() + " by " + mModifiedBy, null);

			/* Actual time on the picked-up item */
			LocalDateTime modTime = LocalDateTime.ofEpochSecond(mModifiedOn, 0, ZoneOffset.UTC);
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			((ListVariable)bosWrap.getVariable("Lore")).add(ChatColor.AQUA + "Modified " + modTime.format(fmt) + " by " + mModifiedBy, null);
		}

		placeholderWrap.save();
		bosWrap.save();
	}

	private String getTimeDeltaStr() {
		long deltaSeconds = Instant.now().getEpochSecond() - mModifiedOn;

		if (deltaSeconds > 60 * 24 * 60 * 60) {
			/* More than 2 months - just print months */
			return Long.toString(deltaSeconds / (60 * 24 * 60 * 60)) + " months ago";
		} else {
			String retStr = "";

			long days = deltaSeconds / (24 * 60 * 60);
			if (days >= 1) {
				retStr += Long.toString(days) + "d ";
			}

			if (days < 7) {
				long hours = (deltaSeconds % (24 * 60 * 60)) / (60 * 60);
				if (hours >= 1) {
					retStr += Long.toString(hours) + "h ";
				}

				if (days == 0) {
					long minutes = (deltaSeconds % (60 * 60)) / 60;
					if (minutes >= 1) {
						retStr += Long.toString(minutes) + "m ";
					}
				}
			}

			return retStr + "ago";
		}
	}


	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		obj.addProperty("mojangson", mNBT.toString());
		obj.addProperty("modified_on", mModifiedOn);
		obj.addProperty("modified_by", mModifiedBy);
		if (mWidth != null && mHeight != null) {
			obj.addProperty("width", mWidth);
			obj.addProperty("height", mHeight);
		}

		return obj;
	}

	public static SoulHistoryEntry fromJson(JsonObject obj, Set<String> locations, List<Component> lore, List<Component> description) throws Exception {
		JsonElement elem = obj.get("mojangson");

		NBTTagCompound nbt = NBTTagCompound.fromString(elem.getAsString());
		long modifiedOn = obj.get("modified_on").getAsLong();
		String modifiedBy = "";
		if (obj.has("modified_by")) {
			modifiedBy = obj.get("modified_by").getAsString();
		}
		Double width = null;
		Double height = null;
		if (obj.has("width") && obj.has("height")) {
			width = obj.get("width").getAsDouble();
			height = obj.get("height").getAsDouble();
		}

		return new SoulHistoryEntry(nbt, modifiedOn, modifiedBy, locations, lore, description, width, height);
	}
}
