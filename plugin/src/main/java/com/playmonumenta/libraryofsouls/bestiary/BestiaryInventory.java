package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import net.md_5.bungee.api.ChatColor;

public class BestiaryInventory extends CustomInventory {

	private List<? extends Soul> mCurrentSlots;
	private List<? extends Soul> mSouls;
	private static String mTitle = "";
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;
	private Region[] mCurrentPoi;
	private static ItemStack mNotFound = new ItemStack(Material.BARRIER);
	private Map<SoulEntry, Integer> mAvailableMobs;

	static {
		ItemMeta meta = mNotFound.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED  + "Mob not discovered!");
		mNotFound.setItemMeta(meta);
	}

	public BestiaryInventory(Player owner, String title) {
		super(owner, 36, "Bestiary: " + BestiaryUtils.hashColor(title) + BestiaryUtils.formatWell(title));
		mTitle = title;
		loadRegionWindow(mTitle);
	}

	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title) {
		this(owner, souls, title, BestiaryManager.getAllKilledMobs(owner, SoulsDatabase.getInstance().getSoulsByLocation(title)));
	}

	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title, Map<SoulEntry, Integer> availableMobs) {
		super(owner, 36, "Bestiary: " + BestiaryUtils.hashColor(title) + BestiaryUtils.formatWell(title));
		mSouls = souls;
		mTitle = title;
		mAvailableMobs = availableMobs;
		if (BestiaryUtils.formatWell(mTitle).equals("Region 1") || BestiaryUtils.formatWell(mTitle).equals("Region 2")) {
			loadRegionWindow(BestiaryUtils.formatWell(mTitle));
		} else {
			loadWindow();
		}
	}

	public void loadWindow() {
		mCurrentSlots = mSouls.subList(mOffset, Math.min(mSouls.size(), mOffset + 27));
		for (int i = 0; i < 26; i++) {
			if (i < mCurrentSlots.size()) {
				if (mAvailableMobs.containsKey(mCurrentSlots.get(i))) {
					_inventory.setItem(i, bestiaryPlaceholder(mCurrentSlots.get(i)));
				} else {
					_inventory.setItem(i, mNotFound);
				}
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

	private void loadRegionWindow(String region) {
		Region[] areasMid = new Region[BestiaryUtils.mPoiMap.size()];
		int rNumber = 0;
		switch(Character.toString(region.charAt(7))) {
		case "1":
			rNumber = 1;
		case "2":
			rNumber = 1;
		}
		int arrayCount = 0;
		for (int track = 0; track < BestiaryUtils.mPoiMap.size(); track++) {
			if (BestiaryUtils.mPoiMap.get(track).getRegionNumber() == rNumber) {
				areasMid[track] = BestiaryUtils.mPoiMap.get(track);
				arrayCount++;
			}
		}

		Region[] areas = new Region[arrayCount];

		for (int i = 0; i < arrayCount; i++) {
			areas[i] = areasMid[i];
		}

		int n = areas.length;
        Region output[] = new Region[n];
        int count[] = new int[256];

        for (int i = 0; i < 256; i++) {
            count[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            count[areas[i].getAreaNumber()]++;
        }

        for (int i = 1; i <= 255; i++) {
            count[i] += count[i-1];
        }

        for (int i = n-1; i >= 0; i--) {
            output[count[areas[i].getAreaNumber()]-1] = areas[i];
            count[areas[i].getAreaNumber()]--;
        }

        for (int i = 0; i < n; i++) {
            areas[i] = output[i];
        }

        for (int i = 0 + mOffset; i < 27 + mOffset; i++) {
        	if (i < areas.length) {
        		ItemStack item = areas[i].getRegionItem();
        		ItemMeta meta = item.getItemMeta();
        		meta.setDisplayName(BestiaryUtils.formatWell(areas[i].getPoiName()));
        		item.setItemMeta(meta);
				_inventory.setItem(i, item);
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

		if (areas.length >= 27) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
			_inventory.setItem(35, null);
		}
	}

	@Override
	protected void inventoryClick (final InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 27) {
			if (event.getClick().equals(ClickType.LEFT) && mCurrentSlots != null) {
				if (event.getCursor().getType().equals(Material.BARRIER)) {
					event.setCancelled(true);
				} else {
					Soul soul = mCurrentSlots.get(slot);
					if (mAvailableMobs.get(soul) < 3 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						event.getWhoClicked().sendMessage(ChatColor.DARK_RED + "You have not gained enough knowledge of this mob!");
						event.setCancelled(true);
					} else if (mAvailableMobs.get(soul) < 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, true, mTitle, slot + mOffset, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
					} else if (mAvailableMobs.get(soul) >= 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, false, mTitle, slot + mOffset, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
					} else if (mAvailableMobs.get(soul) < 5) {
						event.getWhoClicked().sendMessage(ChatColor.DARK_RED + "You have not gained enough knowledge of this mob!");
						event.setCancelled(true);
					} else if (mAvailableMobs.get(soul) < 10) {
						new BestiaryEntry(soul, player, true, mTitle, slot + mOffset, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
					} else {
						new BestiaryEntry(soul, player, false, mTitle, slot + mOffset, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
					}
				}
			} else if (event.getClick().equals(ClickType.LEFT) && mCurrentPoi != null) {
				new BestiaryInventory((Player)event.getWhoClicked(), SoulsDatabase.getInstance().getSoulsByLocation(mCurrentPoi[slot].getArea()), BestiaryUtils.formatWell(mCurrentPoi[slot].getArea()))
						.openInventory((Player)event.getWhoClicked(), LibraryOfSouls.getInstance());
			}
			event.setCancelled(true);
		} else if (slot ==  27 && mHasPrevPage) {
			mOffset -= 27;
			loadWindow();
			event.setCancelled(true);
		} else if (slot == 35 && mHasNextPage) {
			mOffset += 27;
			loadWindow();
			event.setCancelled(true);
		} else if (event.getCursor().getType() == Material.AIR) {
			event.setCancelled(true);
		} else {
			event.setCancelled(true);
		}
	}

	private static ItemStack bestiaryPlaceholder(Soul soul) {
		ItemStack bestiary = new ItemStack(soul.getPlaceholder());
		ItemMeta meta = bestiary.getItemMeta();
		meta.setDisplayName(BestiaryUtils.hashColor(mTitle) + meta.getDisplayName());
		List<String> lore = new ArrayList<String>();
		lore.add(BestiaryUtils.hashColor(mTitle) + BestiaryUtils.formatWell(mTitle));
		lore.add(BestiaryUtils.hashColor(mTitle) + EntityNBT.fromEntityData(soul.getNBT()).getEntityType().toString());
		meta.setLore(lore);
		bestiary.setItemMeta(meta);
		return bestiary;
	}
}
