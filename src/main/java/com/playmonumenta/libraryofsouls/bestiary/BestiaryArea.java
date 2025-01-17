package com.playmonumenta.libraryofsouls.bestiary;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.reflect.NBTUtils;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.utils.Utils;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class BestiaryArea implements BestiaryEntryInterface {
	private final @Nullable BestiaryArea mParent;
	private final Component mName;
	private final @Nullable String mLocation;
	private final @Nullable NamespacedKey mAdvancementKey;
	private final ItemStack mItem;
	private final List<BestiaryEntryInterface> mChildren;

	private static final ItemStack NOT_FOUND_ITEM = new ItemStack(Material.PAPER);

	static {
		ItemMeta meta = NOT_FOUND_ITEM.getItemMeta();
		meta.displayName(Component.text("Area not discovered!", NamedTextColor.DARK_RED, TextDecoration.ITALIC));
		NOT_FOUND_ITEM.setItemMeta(meta);
	}

	public BestiaryArea(@Nullable BestiaryArea parent, String name, ConfigurationSection config) throws Exception {
		mParent = parent;
		mName = Utils.parseMiniMessage(name);

		if (config.contains("location_tag") && config.contains("children")) {
			throw new Exception("Bestiary entry " + Utils.plainText(mName) + " should contain only location_tag OR children, not both");
		} else if (config.contains("location_tag")) {
			mLocation = config.getString("location_tag");
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(mLocation);
			if (souls == null || souls.isEmpty()) {
				throw new Exception("Bestiary entry " + Utils.plainText(mName) + " specifies nonexistent location " + mLocation);
			}
			mChildren = new ArrayList<>(souls);
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
			throw new Exception("Bestiary entry " + Utils.plainText(mName) + " must contain location_tag OR children");
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

		if (config.contains("item")) {
			NBTTagCompound compound = NBTTagCompound.fromString(config.getString("item"));
			compound.setByte("Count", (byte) 1);
			mItem = NBTUtils.itemStackFromNBTData(compound);
			if (mItem == null || mItem.getType().isAir()) {
				throw new Exception("Item for " + Utils.plainText(mName) + " failed to parse, was: " + config.getString("item"));
			}
		} else {
			throw new Exception("Bestiary entry " + Utils.plainText(mName) + " is missing 'item'");
		}

		ItemMeta meta = mItem.getItemMeta();
		meta.displayName(mName.colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

		if (config.contains("subtitle")) {
			Component subtitle = Utils.parseMiniMessage(config.getString("subtitle"));
			meta.lore(List.of(subtitle));
		}

		// Hide weapon damage, book enchants, and potion effects:
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

		mItem.setItemMeta(meta);
	}

	/*--------------------------------------------------------------------------------
	 * BestiaryEntryInterface Interface
	 */

	@Override
	public Component getName() {
		return mName;
	}

	@Override
	public boolean canOpenBestiary(Player player) {
		return mAdvancementKey == null || player.getAdvancementProgress(Bukkit.getAdvancement(mAdvancementKey)).isDone() || player.hasPermission("los.bestiary.viewall");
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
	public void openBestiary(Player player, @Nullable BestiaryArea parent, @Nullable List<BestiaryEntryInterface> peers, int peerIndex) {
		/* Note this ignores the provided parent - the inventory will know to call getBestiaryParent() */
		/* Maybe someday could use peers and peerIndex to go back and forth between areas too? */
		new BestiaryAreaInventory(player, this, 0).openInventory(player, LibraryOfSouls.getInstance());
	}

	/*
	 * BestiaryEntryInterface Interface
	 *--------------------------------------------------------------------------------*/

	/* Note that this is *not* in the interface - because there's no way for a regular mob to know what its parent is */
	public @Nullable BestiaryArea getBestiaryParent() {
		return mParent;
	}

	/* Only intermediate nodes have children */
	public List<BestiaryEntryInterface> getBestiaryChildren() {
		return mChildren;
	}
}
