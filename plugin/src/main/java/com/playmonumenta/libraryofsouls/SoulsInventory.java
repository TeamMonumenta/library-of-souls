package com.playmonumenta.libraryofsouls;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.utils.Utils;

public class SoulsInventory extends CustomInventory {
	private List<? extends Soul> mCurrentSlots;

	private final List<? extends Soul> mSouls;
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;

	public SoulsInventory(Player owner, List<? extends Soul> souls, String titleModifier) {
		super(owner, 54, "Souls Library" + (titleModifier.isEmpty() ? "" : " " + Utils.hashColor(titleModifier)));

		mSouls = souls;
		mOffset = 0;
		loadWindow();
	}

	private void loadWindow() {
		mCurrentSlots = mSouls.subList(mOffset, Math.min(mSouls.size(), mOffset + 36));

		for (int i = 0; i < 36; i++) {
			if (i < mCurrentSlots.size()) {
				_inventory.setItem(i, mCurrentSlots.get(i).getPlaceholder());
			} else {
				_inventory.setItem(i, null);
			}
		}

		if (mOffset > 0) {
			_inventory.setItem(45, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 36) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
			_inventory.setItem(45, null);
		}

		if (mCurrentSlots.size() >= 36) {
			_inventory.setItem(53, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 36) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
			_inventory.setItem(53, null);
		}
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 36) {
			if (event.isShiftClick()) {
				event.setCurrentItem(mCurrentSlots.get(slot).getBoS());
				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						event.setCurrentItem(mCurrentSlots.get(slot).getPlaceholder());
					}
				});
			} else {
				if (event.getCursor().getType() == Material.AIR) {
					event.getView().setCursor(mCurrentSlots.get(slot).getBoS());
				}
				event.setCancelled(true);
			}
		} else if (slot == 45 && mHasPrevPage) {
			mOffset -= 36;
			loadWindow();
			event.setCancelled(true);
		} else if (slot == 53 && mHasNextPage) {
			mOffset += 36;
			loadWindow();
			event.setCancelled(true);
		} else if (event.getCursor().getType() == Material.AIR) {
			event.setCancelled(true);
		}
	}

	@Override
	protected void inventoryClose(InventoryCloseEvent event) { }
}
