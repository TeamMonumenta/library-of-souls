package com.playmonumenta.libraryofsouls;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTTagList;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.ItemStackNBTWrapper;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ListVariable;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.playmonumenta.libraryofsouls.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class SoulHistoryEntry implements Soul {
	private static Gson gson = null;

	private final NBTTagCompound mNBT;
	private final long mModifiedOn;
	private final String mModifiedBy;
	private final String mName;
	private final String mLabel;
	private final Set<String> mLocs;
	private final NamespacedKey mId;
	private ItemStack mPlaceholder = null;
	private ItemStack mBoS = null;

	/* Create a SoulHistoryEntry object with existing history */
	public SoulHistoryEntry(NBTTagCompound nbt, long modifiedOn, String modifiedBy, Set<String> locations) throws Exception {
		mNBT = nbt;
		mModifiedOn = modifiedOn;
		mModifiedBy = modifiedBy;
		mLocs = locations;
		mId = EntityNBT.fromEntityData(mNBT).getEntityType().getKey();

		mName = nbt.getString("CustomName");
		mLabel = Utils.getLabelFromName(mName);
		if (mLabel == null || mLabel.isEmpty()) {
			throw new Exception("Refused to load Library of Souls mob with no name!");
		}
	}

	/* Create a new SoulHistoryEntry object from NBT */
	public SoulHistoryEntry(Player player, NBTTagCompound nbt) throws Exception {
		this(nbt, Instant.now().getEpochSecond(), player.getName(), new HashSet<String>());
	}

	/*--------------------------------------------------------------------------------
	 * Soul Interface
	 */

	@Override
	public NBTTagCompound getNBT() {
		return mNBT;
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
	public ItemStack getPlaceholder() {
		if (mPlaceholder == null) {
			regenerateItems();
		}
		return mPlaceholder;
	}

	@Override
	public ItemStack getBoS() {
		if (mBoS == null) {
			regenerateItems();
		}
		return mBoS;
	}

	@Override
	public NamespacedKey getId() {
		return mId;
	}

	@Override
	public String getName() {
		return mName;
	}

	public String getDisplayName() {
		return (isElite() ? ChatColor.GOLD : ChatColor.WHITE) + "" + ChatColor.BOLD + Utils.stripColorsAndJSON(mName);
	}

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

	public String getLabel() {
		return mLabel;
	}

	@Override
	public Entity summon(Location loc) {
		return EntityNBT.fromEntityData(mNBT).spawn(loc);
	}

	/*
	 * Soul Interface
	 *--------------------------------------------------------------------------------*/

	private List<String> stringifyWrapList(String prefix, int maxLen, Object[] elements) {
		List<String> ret = new LinkedList<String>();

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
			mBoS = (new BookOfSouls(entityNBT)).getBook();
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

		switch (entityNBT.getEntityType()) {
			case BLAZE:
				mPlaceholder = new ItemStack(Material.BLAZE_POWDER);
				break;
			case CAVE_SPIDER:
				mPlaceholder = new ItemStack(Material.FERMENTED_SPIDER_EYE);
				break;
			case CHICKEN:
				mPlaceholder = new ItemStack(Material.CHICKEN);
				break;
			case COW:
				mPlaceholder = new ItemStack(Material.BEEF);
				break;
			case CREEPER:
				mPlaceholder = new ItemStack(Material.CREEPER_HEAD);
				break;
			case DOLPHIN:
				mPlaceholder = new ItemStack(Material.COD);
				break;
			case DROWNED:
				mPlaceholder = new ItemStack(Material.TRIDENT);
				break;
			case ELDER_GUARDIAN:
				mPlaceholder = new ItemStack(Material.SPONGE);
				break;
			case ENDERMAN:
				mPlaceholder = new ItemStack(Material.ENDER_PEARL);
				break;
			case EVOKER:
				mPlaceholder = new ItemStack(Material.TOTEM_OF_UNDYING);
				break;
			case GHAST:
				mPlaceholder = new ItemStack(Material.GHAST_TEAR);
				break;
			case GUARDIAN:
				mPlaceholder = new ItemStack(Material.PUFFERFISH);
				break;
			case HUSK:
				mPlaceholder = new ItemStack(Material.ROTTEN_FLESH);
				break;
			case ILLUSIONER:
				mPlaceholder = new ItemStack(Material.BOW);
				break;
			case IRON_GOLEM:
				mPlaceholder = new ItemStack(Material.IRON_BLOCK);
				break;
			case MAGMA_CUBE:
				mPlaceholder = new ItemStack(Material.MAGMA_CREAM);
				break;
			case OCELOT:
				mPlaceholder = new ItemStack(Material.CHICKEN);
				break;
			case PHANTOM:
				mPlaceholder = new ItemStack(Material.PHANTOM_MEMBRANE);
				break;
			case PIG_ZOMBIE:
				mPlaceholder = new ItemStack(Material.GOLD_NUGGET);
				break;
			case SHULKER:
				mPlaceholder = new ItemStack(Material.SHULKER_BOX);
				break;
			case SILVERFISH:
				mPlaceholder = new ItemStack(Material.MOSSY_STONE_BRICKS);
				break;
			case SKELETON:
				mPlaceholder = new ItemStack(Material.SKELETON_SKULL);
				break;
			case SLIME:
				mPlaceholder = new ItemStack(Material.SLIME_BALL);
				break;
			case SNOWMAN:
				mPlaceholder = new ItemStack(Material.CARVED_PUMPKIN);
				break;
			case SPIDER:
				mPlaceholder = new ItemStack(Material.SPIDER_EYE);
				break;
			case STRAY:
				mPlaceholder = new ItemStack(Material.BOW);
				break;
			case SQUID:
				mPlaceholder = new ItemStack(Material.INK_SAC);
				break;
			case VEX:
				mPlaceholder = new ItemStack(Material.IRON_SWORD);
				break;
			case VINDICATOR:
				mPlaceholder = new ItemStack(Material.STONE_AXE);
				break;
			case WITCH:
				mPlaceholder = new ItemStack(Material.POISONOUS_POTATO);
				break;
			case WITHER:
				mPlaceholder = new ItemStack(Material.NETHER_STAR);
				break;
			case WITHER_SKELETON:
				mPlaceholder = new ItemStack(Material.WITHER_SKELETON_SKULL);
				break;
			case WOLF:
				mPlaceholder = new ItemStack(Material.BONE);
				break;
			case ZOMBIE:
				mPlaceholder = new ItemStack(Material.ZOMBIE_HEAD);
				break;
			case ZOMBIE_VILLAGER:
				mPlaceholder = new ItemStack(Material.BELL);
				break;
			default:
				mPlaceholder = mBoS.clone();
				break;
		}

		mPlaceholder = mPlaceholder.ensureServerConversions();
		mBoS = mBoS.ensureServerConversions();

		ItemStackNBTWrapper placeholderWrap = new ItemStackNBTWrapper(mPlaceholder);
		ItemStackNBTWrapper bosWrap = new ItemStackNBTWrapper(mBoS);

		/* Set the item's display name (recolored, does not exactly match actual mob name) */
		placeholderWrap.getVariable("Name").set(getDisplayName(), null);
		bosWrap.getVariable("Name").set(getDisplayName(), null);

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

			for (String str : stringifyWrapList("  ", 50, tags.getAsArray())) {
				((ListVariable)placeholderWrap.getVariable("Lore")).add(str, null);
				((ListVariable)bosWrap.getVariable("Lore")).add(str, null);
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

		obj.add("mojangson", new JsonPrimitive(mNBT.toString()));
		obj.add("modified_on", new JsonPrimitive(mModifiedOn));
		obj.add("modified_by", new JsonPrimitive(mModifiedBy));

		return obj;
	}

	public static SoulHistoryEntry fromJson(JsonObject obj, Set<String> locations) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		JsonElement elem = obj.get("mojangson");

		NBTTagCompound nbt = NBTTagCompound.fromString(elem.getAsString());
		long modifiedOn = obj.get("modified_on").getAsLong();
		String modifiedBy = "";
		if (obj.has("modified_by")) {
			modifiedBy = obj.get("modified_by").getAsString();
		}

		return new SoulHistoryEntry(nbt, modifiedOn, modifiedBy, locations);
	}
}
