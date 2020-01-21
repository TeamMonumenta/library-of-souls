package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTTagList;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;

import net.md_5.bungee.api.ChatColor;

public class SoulsDatabase {
	public static class SoulSlot {
		private final ItemStack mPlaceholder;
		private final ItemStack mBoS;

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

			ItemMeta placeholderMeta = mPlaceholder.getItemMeta();
			ItemMeta bosMeta = mBoS.getItemMeta();

			List<String> lore = new ArrayList<String>();

			/* TODO: Fix this custom name being JSON ugly */
			String name = nbt.getString("CustomName");
			if (name != null) {
				placeholderMeta.setDisplayName(name);
				bosMeta.setDisplayName(name);
			}

			if (nbt.hasKey("Health")) {
				lore.add(ChatColor.WHITE + "Health: " + Double.toString(nbt.getDouble("Health")));
			}

			NBTTagList tags = nbt.getList("Tags");
			if (tags != null && tags.size() > 0) {
				lore.add(ChatColor.WHITE + "Tags: " + tags.toString());
			}

			placeholderMeta.setLore(lore);
			bosMeta.setLore(lore);

			mPlaceholder.setItemMeta(placeholderMeta);
			mBoS.setItemMeta(bosMeta);
		}

		public ItemStack getPlaceholder() {
			return mPlaceholder;
		}

		public ItemStack getBoS() {
			return mBoS;
		}
	}
	private static SoulsDatabase INSTANCE = null;

	private List<NBTTagCompound> mSouls = new ArrayList<NBTTagCompound>();

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

		return new SoulSlot(mSouls.get(index));
	}

	/* TODO: File watcher */
	public void reload(Plugin plugin) throws Exception {
		mSouls = new ArrayList<NBTTagCompound>();

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

			mSouls.add(NBTTagCompound.fromString(elem.getAsString()));
			count++;
		}
		plugin.getLogger().info("Loaded " + Integer.toString(count) + " mob souls");
	}

	public static SoulsDatabase getInstance() {
		return INSTANCE;
	}
}
