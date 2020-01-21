package com.playmonumenta.libraryofsouls;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;

public class SoulsInventory extends CustomInventory {
	private final SoulsDatabase mDB;

	public SoulsInventory(Player owner) {
		super(owner, 54, "Library of Souls");

		mDB = SoulsDatabase.getInstance();

		for (ItemStack item : mDB.getSouls(0, 36)) {
			_inventory.addItem(item);
		}
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 54) {
			final ItemStack item = event.getCurrentItem().clone();
			if (event.isShiftClick()) {
				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						event.setCurrentItem(item);
					}
				});
			} else {
				if (event.getCursor().getType() == Material.AIR) {
					event.getView().setCursor(item);
				}
				event.setCancelled(true);
			}
		} else if (event.getCursor().getType() == Material.AIR) {
			event.setCancelled(true);
		}
	}

	@Override
	protected void inventoryClose(InventoryCloseEvent event) { }
}
