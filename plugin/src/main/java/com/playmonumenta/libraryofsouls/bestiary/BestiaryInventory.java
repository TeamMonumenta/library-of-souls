package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;

import net.md_5.bungee.api.ChatColor;

public class BestiaryInventory extends CustomInventory{

	private List<? extends Soul> mCurrentSlots;
	private List<? extends Soul> mSouls;
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;

	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title) {
		super(owner, 36, "Bestiary: " + hashColor(title));
	}

	public void loadWindow() {
		mCurrentSlots = mSouls.subList(mOffset, Math.min(mSouls.size(), mOffset + 27));

		for (int i = 0; i < 26; i++) {
			if (i < mCurrentSlots.size()) {
				_inventory.setItem(i, bestiaryPlaceholder(mCurrentSlots.get(i)));
			} else {
				_inventory.setItem(i, null);
			}
		}

		if (mOffset > 0) {
			_inventory.setItem(27, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 27) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
			_inventory.setItem(27, null);
		}

		if (mCurrentSlots.size() >= 27) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
			_inventory.setItem(35, null);
		}
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {

	}

	private static ItemStack bestiaryPlaceholder(Soul soul) {
		ItemStack bestiary = new ItemStack(soul.getPlaceholder().getType());
		ItemMeta meta = bestiary.getItemMeta();
		meta.setDisplayName(soul.getPlaceholder().getItemMeta().getDisplayName());
		List<String> lore = new ArrayList<String>();
		lore.add(soul.getName());
		meta.setLore(lore);
		bestiary.setItemMeta(meta);
		return bestiary;
	}

	private static String hashColor(String in) {
		switch(in) {
		case "white":
			return ChatColor.WHITE + in;
		case "orange":
			return ChatColor.GOLD + in;
		case "magenta":
			return ChatColor.LIGHT_PURPLE + in;
		case "lightblue":
			return ChatColor.BLUE + in;
		case "yellow":
			return ChatColor.YELLOW + in;
		case "lime":
			return ChatColor.GREEN + in;
		case "pink":
			return ChatColor.RED + in;
		case "gray":
			return ChatColor.DARK_GRAY + in;
		case "lightgray":
			return ChatColor.GRAY + in;
		case "cyan":
			return ChatColor.AQUA + in;
		case "purple":
			return ChatColor.DARK_PURPLE + in;
		case "blue":
			return ChatColor.BLUE + in;
		case "brown":
			return ChatColor.DARK_AQUA + in;
		case "green":
			return ChatColor.GREEN + in;
		case "red":
			return ChatColor.DARK_RED + in;
		case "black":
			return ChatColor.BLACK + in;
		case "shiftingcity":
			return ChatColor.AQUA + in;
		case "willows":
			return ChatColor.DARK_GREEN + in;
		case "reverie":
			return ChatColor.DARK_RED + in;
		case "roguelike":
			return ChatColor.DARK_RED + in;
		default:
			return ChatColor.BLACK + in;
		}
	}
}
