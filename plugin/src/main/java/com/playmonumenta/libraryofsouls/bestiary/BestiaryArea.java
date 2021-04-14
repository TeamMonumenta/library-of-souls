package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTUtils;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BestiaryArea implements BestiaryEntryInterface {
	private final BestiaryArea mParent;
	private final String mName;
	private final String mLocation;
	private final String mSubtitle;
	private final NamespacedKey mAdvancementKey;
	private final ItemStack mItem;
	private final List<BestiaryEntryInterface> mChildren;

	private static final ItemStack NOT_FOUND_ITEM = new ItemStack(Material.PAPER);
	static {
		ItemMeta meta = NOT_FOUND_ITEM.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED  + "Area not discovered!");
		NOT_FOUND_ITEM.setItemMeta(meta);
	}

	public BestiaryArea(BestiaryArea parent, String name, ConfigurationSection config) throws Exception {
		mParent = parent;
		mName = name;

		if (config.contains("location_tag") && config.contains("children")) {
			throw new Exception("Bestiary entry " + mName + " should contain only location_tag OR children, not both");
		} else if (config.contains("location_tag")) {
			mLocation = config.getString("location_tag");
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(mLocation);
			if (souls == null || souls.isEmpty()) {
				throw new Exception("Bestiary entry " + mName + " specifies nonexistent location " + mLocation);
			}
			mChildren = new ArrayList<BestiaryEntryInterface>(souls);
		} else if (config.contains("children")) {
			mLocation = null;

			ConfigurationSection children = config.getConfigurationSection("children");
			Set<String> childKeys = children.getKeys(false);
			mChildren = new ArrayList<>(childKeys.size());
			for (String childKey : childKeys) {
				try {
					mChildren.add(new BestiaryArea(this, childKey, children.getConfigurationSection(childKey)));
				} catch (Exception ex) {
					LibraryOfSouls.getInstance().getLogger().warning("Failed to load bestiary area " + childKey + ": " + ex.getMessage());
				}
			}
		} else {
			throw new Exception("Bestiary entry " + mName + " must contain location_tag OR children");
		}

		if (config.contains("required_advancement")) {
			mAdvancementKey = NamespacedKey.fromString(config.getString("required_advancement"));
			/* Try to load the advancement */
			try {
				if (Bukkit.getAdvancement(mAdvancementKey) == null) {
					throw new Exception("Bestiary advancement " + mAdvancementKey + " does not exist!");
				}
			} catch (Exception ex) {
				/* This message is really ugly otherwise */
				throw new Exception("Bestiary advancement " + mAdvancementKey + " does not exist!");
			}
		} else {
			mAdvancementKey = null;
		}

		if (config.contains("subtitle")) {
			mSubtitle = config.getString("subtitle");
		} else {
			mSubtitle = null;
		}

		if (config.contains("item")) {
			NBTTagCompound compound = NBTTagCompound.fromString(config.getString("item"));
			compound.setByte("Count", (byte)1);
			mItem = NBTUtils.itemStackFromNBTData(compound);
			if (mItem == null || mItem.getType().isAir()) {
				throw new Exception("Item for " + mName + " failed to parse, was: " + config.getString("item"));
			}
		} else {
			throw new Exception("Bestiary entry " + mName + " is missing 'item'");
		}

		ItemMeta meta = mItem.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + mName);
		if (mSubtitle != null) {
			meta.setLore(Arrays.asList(mSubtitle));
		}
		mItem.setItemMeta(meta);
	}

	/*--------------------------------------------------------------------------------
	 * BestiaryEntryInterface Interface
	 */

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public boolean canOpenBestiary(Player player) {
		return mAdvancementKey == null || player.getAdvancementProgress(Bukkit.getAdvancement(mAdvancementKey)).isDone();
	}

	@Override
	public ItemStack getBestiaryItem(Player player) {
		if (canOpenBestiary(player)) {
			/* Advancement not required OR player has completed it */
			return mItem;
		} else {
			/* Advancement is required but player doesn't have it */
			return NOT_FOUND_ITEM;
		}
	}

	@Override
	public void openBestiary(Player player, BestiaryArea parent) {
		/* Note this ignores the provided parent - the inventory will know to call getBestiaryParent() */
		new BestiaryAreaInventory(player, this, 0).openInventory(player, LibraryOfSouls.getInstance());
	}

	/*
	 * BestiaryEntryInterface Interface
	 *--------------------------------------------------------------------------------*/

	/* Note that this is *not* in the interface - because there's no way for a regular mob to know what its parent is */
	public BestiaryArea getBestiaryParent() {
		return mParent;
	}

	/* Only intermediate nodes have children */
	public List<BestiaryEntryInterface> getBestiaryChildren() {
		return mChildren;
	}
}