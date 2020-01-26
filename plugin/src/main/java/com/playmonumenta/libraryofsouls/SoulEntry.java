package com.playmonumenta.libraryofsouls;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTTagList;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.ItemStackNBTWrapper;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ListVariable;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.playmonumenta.libraryofsouls.utils.Utils;

public class SoulEntry {
	private static Gson gson = null;

	private final Set<String> mLocs;
	private final NBTTagCompound mNBT;
	private final String mName;
	private final String mLabel;
	private ItemStack mPlaceholder = null;
	private ItemStack mBoS = null;

	public SoulEntry(JsonObject obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		JsonElement elem = obj.get("mojangson");

		mNBT = NBTTagCompound.fromString(elem.getAsString());

		mName = mNBT.getString("CustomName");
		try {
			mLabel = Utils.stripColorsAndJSON(gson, mName).replaceAll(" ", "");
		} catch (Exception e) {
			throw new Exception("Failed to parse Library of Souls mob name '" + mName + "'");
		}
		if (mLabel == null || mLabel.isEmpty()) {
			throw new Exception("Refused to load Library of Souls mob with no name!");
		}

		mLocs = new HashSet<String>();
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
					throw new Exception("location_names entry for '" + mName + "' is not a string!");
				}
				mLocs.add(tagElement.getAsString());
			}
		}
	}

	private void regenerateItems() {
		EntityNBT entityNBT = EntityNBT.fromEntityData(mNBT);

		mBoS = (new BookOfSouls(entityNBT)).getBook();

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
			case DROWNED:
				mPlaceholder = new ItemStack(Material.TRIDENT);
				break;
			case ENDERMAN:
				mPlaceholder = new ItemStack(Material.ENDER_PEARL);
				break;
			case GHAST:
				mPlaceholder = new ItemStack(Material.GHAST_TEAR);
				break;
			case GUARDIAN:
				mPlaceholder = new ItemStack(Material.PUFFERFISH);
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
			case PHANTOM:
				mPlaceholder = new ItemStack(Material.PHANTOM_MEMBRANE);
				break;
			case SHULKER:
				mPlaceholder = new ItemStack(Material.SHULKER_BOX);
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
			case VINDICATOR:
				mPlaceholder = new ItemStack(Material.STONE_AXE);
				break;
			case WITCH:
				mPlaceholder = new ItemStack(Material.POISONOUS_POTATO);
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
			default:
				mPlaceholder = mBoS.clone();
				break;
		}

		mPlaceholder = mPlaceholder.ensureServerConversions();
		mBoS = mBoS.ensureServerConversions();

		ItemStackNBTWrapper placeholderWrap = new ItemStackNBTWrapper(mPlaceholder);
		ItemStackNBTWrapper bosWrap = new ItemStackNBTWrapper(mBoS);

		/* Set the item's display name (force json name if source mob has json name) */
		placeholderWrap.getVariable("Name").set(mName, null);
		bosWrap.getVariable("Name").set(mName, null);

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

		placeholderWrap.save();
		bosWrap.save();
	}

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

	public ItemStack getPlaceholder() {
		if (mPlaceholder == null) {
			regenerateItems();
		}
		return mPlaceholder;
	}

	public ItemStack getBoS() {
		if (mBoS == null) {
			regenerateItems();
		}
		return mBoS;
	}

	/* This is the full name, with colors, spaces, and possibly JSON */
	public String getName() {
		return mName;
	}

	/* This is the label-ified name, with colors and spaces stripped */
	public String getLabel() {
		return mLabel;
	}

	public Set<String> getLocationNames() {
		return mLocs;
	}

	public NBTTagCompound getNBT() {
		return mNBT;
	}

	public JsonObject serialize() {
		JsonObject obj = new JsonObject();

		obj.add("mojangson", new JsonPrimitive(mNBT.toString()));

		JsonArray array = new JsonArray();
		for (String location : mLocs) {
			array.add(location);
		}

		obj.add("location_names", array);

		return obj;
	}
}

