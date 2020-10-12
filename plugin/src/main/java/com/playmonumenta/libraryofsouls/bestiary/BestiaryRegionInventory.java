package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import net.md_5.bungee.api.ChatColor;

public class BestiaryRegionInventory extends CustomInventory {
	private static String mTitle = "";
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;
	private Region[] mCurrentPois;
	private static ItemStack mNotFound = new ItemStack(Material.PAPER);
	private static ItemStack mGoBack = new ItemStack(Material.RED_STAINED_GLASS_PANE);


	static {
		ItemMeta meta = mNotFound.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED  + "Mob not discovered!");
		mNotFound.setItemMeta(meta);

		meta = mGoBack.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Go Back");
		mGoBack.setItemMeta(meta);
	}

	public BestiaryRegionInventory(Player owner, String title) {
		this(owner, title, null, 0);
	}

	//To load it from a certain place
	public BestiaryRegionInventory(Player owner, String title, Region[] pois, int offset) {
		super(owner, 36, ChatColor.BLACK + "Bestiary: " + BestiaryUtils.hashColor(title) + BestiaryUtils.formatWell(title));
		mTitle = title;
		mCurrentPois = pois;
		mOffset = offset;
		loadRegionWindow(mTitle, owner);
	}

	private void loadRegionWindow(String region, Player player) {
		if (mCurrentPois != null) {
			for (int i = 0 + mOffset; i < 27 + mOffset; i++) {
		        if (i < mCurrentPois.length) {
		        	if (player.getAdvancementProgress(mCurrentPois[i].getAdvancement()).isDone() || player.getGameMode() == GameMode.CREATIVE) {
			        	ItemStack item = mCurrentPois[i].getRegionItem();
			        	ItemMeta meta = item.getItemMeta();
			        	meta.setDisplayName(ChatColor.WHITE + BestiaryUtils.formatWell(mCurrentPois[i].getPoiName()));
			        	item.setItemMeta(meta);
						_inventory.setItem(i - mOffset, item);
		        	} else {
		        		_inventory.setItem(i, mNotFound);
		        	}

				} else {
					_inventory.setItem(i - mOffset, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
				}
		     }
		} else {
			Region[] areasMid = new Region[BestiaryUtils.mPoiMap.size()];
			int rNumber = Integer.valueOf(Character.toString(region.charAt(7)));
			int arrayCount = 0;
			for (int track = 0; track < BestiaryUtils.mPoiMap.size(); track++) {
				if (BestiaryUtils.mPoiMap.get(track).getRegionNumber() == rNumber) {
					areasMid[arrayCount] = BestiaryUtils.mPoiMap.get(track);
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
	        mCurrentPois = areas;
	        for (int i = 0 + mOffset; i < 27 + mOffset; i++) {
	        	if (i < areas.length) {
	        		if (player.getAdvancementProgress(mCurrentPois[i].getAdvancement()).isDone() || player.getGameMode() == GameMode.CREATIVE) {
			        	ItemStack item = mCurrentPois[i].getRegionItem();
			        	ItemMeta meta = item.getItemMeta();
			        	meta.setDisplayName(ChatColor.WHITE + BestiaryUtils.formatWell(mCurrentPois[i].getPoiName()));
			        	item.setItemMeta(meta);
						_inventory.setItem(i - mOffset, item);
		        	} else {
		        		_inventory.setItem(i, mNotFound);
		        	}
				} else {
					_inventory.setItem(i - mOffset, null);
				}

	        }
		}

		for (int i = 27; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
		}

        if (mOffset > 0) {
			_inventory.setItem(27, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
		}

		if (27 + mOffset < mCurrentPois.length) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
		}

		_inventory.setItem(31, mGoBack);
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		int slot = event.getRawSlot();
		if (slot >= 0 && slot < 27 && slot < mCurrentPois.length + mOffset) {
			if (event.getClick().equals(ClickType.LEFT) && mCurrentPois != null && slot < mCurrentPois.length) {
				try {
					new BestiaryInventory(player, SoulsDatabase.getInstance().getSoulsByLocation(mCurrentPois[slot + mOffset].getPoiName()),
							BestiaryManager.getAllKilledMobs(player, SoulsDatabase.getInstance().getSoulsByLocation(mCurrentPois[slot + mOffset].getPoiName())), mCurrentPois[slot + mOffset].getPoiName(), mCurrentPois, mOffset, mTitle)
							.openInventory(player, LibraryOfSouls.getInstance());
				} catch (Exception ex) {
					LibraryOfSouls.getInstance().getLogger().severe("Caught error in BestiaryInventory: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
			event.setCancelled(true);
		} else if (slot == 27 && mHasPrevPage) {
			mOffset -= 27;
			loadRegionWindow(mTitle, player);
			event.setCancelled(true);
		} else if (slot == 31 && event.getCurrentItem().equals(mGoBack)) {
			new BestiarySelection(player).openInventory(player, LibraryOfSouls.getInstance());
		} else if (slot == 35 && mHasNextPage) {
			mOffset += 27;
			loadRegionWindow(mTitle, player);
			event.setCancelled(true);
		} else {
			event.setCancelled(true);
		}
	}
}
