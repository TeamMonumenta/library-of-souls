package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
import com.playmonumenta.libraryofsouls.utils.FileUtils;

import net.md_5.bungee.api.ChatColor;

public class SoulsDatabase {
	public static class SoulSlot {
		private ItemStack mPlaceholder;
		private ItemStack mBoS;

		private SoulSlot(NBTTagCompound nbt) {
			EntityNBT entityNBT = EntityNBT.fromEntityData(nbt);

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
				case WITHER:
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
			String name = nbt.getString("CustomName");
			if (name != null) {
				placeholderWrap.getVariable("Name").set(name, null);
				bosWrap.getVariable("Name").set(name, null);
			}

			/* Set hide flags to hide the BoS author info */
			placeholderWrap.getVariable("HideFlags").set("32", null);
			bosWrap.getVariable("HideFlags").set("32", null);

			if (nbt.hasKey("Health")) {
				String healthStr = ChatColor.WHITE + "Health: " + Double.toString(nbt.getDouble("Health"));
				((ListVariable)placeholderWrap.getVariable("Lore")).add(healthStr, null);
				((ListVariable)bosWrap.getVariable("Lore")).add(healthStr, null);
			}

			NBTTagList tags = nbt.getList("Tags");
			if (tags != null && tags.size() > 0) {
				String tagStr = ChatColor.WHITE + "Tags: " + tags.toString();
				((ListVariable)placeholderWrap.getVariable("Lore")).add(tagStr, null);
				((ListVariable)bosWrap.getVariable("Lore")).add(tagStr, null);
			}

			placeholderWrap.save();
			bosWrap.save();
		}

		public ItemStack getPlaceholder() {
			return mPlaceholder;
		}

		public ItemStack getBoS() {
			return mBoS;
		}
	}
	private static SoulsDatabase INSTANCE = null;

	private static final Comparator<String> COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String e1, String e2) {
			return e1.toLowerCase().compareTo(e2.toLowerCase());
		}
	};
	private TreeMap<String, NBTTagCompound> mSouls = new TreeMap<String, NBTTagCompound>(COMPARATOR);

	public SoulsDatabase(Plugin plugin) throws Exception {
		reload(plugin);

		INSTANCE = this;
	}

	public List<SoulSlot> getSouls(int offset, int count) {
		List<SoulSlot> souls = new ArrayList<SoulSlot>(count);

		for (int i = offset; i < offset + count; i++) {
			SoulSlot bos = getSoul(i);
			if (bos != null) {
				souls.add(bos);
			}
		}

		return souls;
	}

	public SoulSlot getSoul(int index) {
		if (index >= mSouls.size()) {
			return null;
		}

		return new SoulSlot((NBTTagCompound)mSouls.values().toArray()[index]);
	}

	public SoulSlot getSoul(String name) {
		NBTTagCompound nbt = mSouls.get(name);
		if (nbt != null) {
			return new SoulSlot(nbt);
		}
		return null;
	}

	/* TODO: File watcher */
	public void reload(Plugin plugin) throws Exception {
		plugin.getLogger().info("Parsing souls library...");
		mSouls = new TreeMap<String, NBTTagCompound>(COMPARATOR);

		File directory = plugin.getDataFolder();
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String content = FileUtils.readFile(Paths.get(plugin.getDataFolder().getPath(), "souls_database.json").toString());
		if (content == null || content.isEmpty()) {
			throw new Exception("Failed to parse file as JSON object");
		}

		Gson gson = new Gson();
		JsonArray array = gson.fromJson(content, JsonArray.class);
		if (array == null) {
			throw new Exception("Failed to parse file as JSON array");
		}

		int count = 0;
		Iterator<JsonElement> iter = array.iterator();
		while (iter.hasNext()) {
			JsonElement entry = iter.next();

			JsonObject obj = entry.getAsJsonObject();
			JsonElement elem = obj.get("mojangson");

			NBTTagCompound nbt = NBTTagCompound.fromString(elem.getAsString());

			String name = nbt.getString("CustomName");
			try {
				name = stripColorsAndJSON(gson, name);
			} catch (Exception e) {
				plugin.getLogger().severe("Failed to parse Library of Souls mob name '" + name + "'");
				continue;
			}
			if (name == null || name.isEmpty()) {
				plugin.getLogger().severe("Refused to load Library of Souls mob with no name!");
				continue;
			}

			name = name.replaceAll(" ", "");

			if (mSouls.get(name) != null) {
				plugin.getLogger().severe("Refused to load Library of Souls duplicate mob '" + name + "'");
				continue;
			}

			plugin.getLogger().info("  " + name);

			mSouls.put(name, nbt);
			count++;
		}
		plugin.getLogger().info("Finished parsing souls library");
		plugin.getLogger().info("Loaded " + Integer.toString(count) + " mob souls");
	}

	public static SoulsDatabase getInstance() {
		return INSTANCE;
	}

	/*
	 * Valid examples:
	 *   §6Master Scavenger
	 *   "§6Master Scavenger"
	 *   "{\"text\":\"§6Master Scavenger\"}"
	 */
	public static String stripColorsAndJSON(Gson gson, String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		JsonElement element = gson.fromJson(str, JsonElement.class);
		return stripColorsAndJSON(element);
	}

	public static String stripColorsAndJSON(JsonElement element) {
		String str = "";
		if (element.isJsonObject()) {
			JsonElement textElement = element.getAsJsonObject().get("text");
			if (textElement != null) {
				str = textElement.getAsString();
			}
		} else if (element.isJsonArray()) {
			str = "";
			for (JsonElement arrayElement : element.getAsJsonArray()) {
				str += stripColorsAndJSON(arrayElement);
			}
		} else {
			str = element.getAsString();
		}
		return ChatColor.stripColor(str);
	}

	public Set<String> listMobNames() {
		return mSouls.keySet();
	}
}
