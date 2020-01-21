package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.libraryofsouls.utils.FileUtils;

public class SoulsDatabase {
	private static SoulsDatabase INSTANCE = null;

	private List<NBTTagCompound> mSouls = new ArrayList<NBTTagCompound>();

	public SoulsDatabase(Plugin plugin) throws Exception {
		reload(plugin);

		INSTANCE = this;
	}

	public List<ItemStack> getSouls(int offset, int count) {
		List<ItemStack> souls = new ArrayList<ItemStack>(count);

		for (int i = offset; i < offset + count; i++) {
			ItemStack bos = getSoul(i);
			if (bos != null) {
				souls.add(bos);
			}
		}

		return souls;
	}

	public ItemStack getSoul(int index) {
		if (index >= mSouls.size()) {
			return null;
		}

		NBTTagCompound nbt = mSouls.get(index);

		ItemStack book = (new BookOfSouls(EntityNBT.fromEntityData(nbt))).getBook();

		/* TODO: Fancy up the book item a bit with more info - maybe a display item and a book item? */

		return book;
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
