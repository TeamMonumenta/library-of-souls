package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.utils.Utils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SoulsInventory extends CustomInventory {
	private List<? extends Soul> mCurrentSlots;

	private final List<? extends Soul> mSouls;
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;
	private final String mTitleModifier;

	public SoulsInventory(Player owner, List<? extends Soul> souls, String titleModifier) {
		super(owner, 54, "Souls Library" + (titleModifier.isEmpty() ? "" : " " + Utils.hashColor(titleModifier)));

		mTitleModifier = titleModifier;
		mSouls = souls;
		mOffset = 0;
		loadWindow();
	}

	public SoulsInventory(SoulsInventory other, Player owner) {
		super(owner, 54, "Souls Library" + (other.mTitleModifier.isEmpty() ? "" : " " + Utils.hashColor(other.mTitleModifier)));

		mTitleModifier = other.mTitleModifier;
		mSouls = other.mSouls;
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
		if (event.getClickedInventory() == null) {
			// Player clicked off the screen
			return;
		} else if ((event.getCursor().getType() != Material.AIR && event.getClickedInventory().equals(getInventory()))
		           || (event.isShiftClick() && !event.getClickedInventory().equals(getInventory()))) {
			// Attempted to place something in the souls inventory
			event.setCancelled(true);
		} else if (event.getClickedInventory().equals(getInventory())) {
			// Clicked in the SoulsInventory
			final int slot = event.getRawSlot();
			if (slot >= 0 && slot < 36 && mCurrentSlots.size() > slot) {
				if (event.isShiftClick()) {
					event.setCurrentItem(mCurrentSlots.get(slot).getBoS());
					Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
						@Override
						public void run() {
							event.setCurrentItem(mCurrentSlots.get(slot).getPlaceholder());
						}
					});
				} else if (event.isRightClick()) {
					event.setCancelled(true);
					SpawnerInventory.openSpawnerInventory(mCurrentSlots.get(slot), (Player)event.getWhoClicked(), this);
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
	}
}
