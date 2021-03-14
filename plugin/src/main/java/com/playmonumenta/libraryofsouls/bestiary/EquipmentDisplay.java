package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;

import net.md_5.bungee.api.ChatColor;

public class EquipmentDisplay extends CustomInventory {

	private Soul mSoul;
	private BestiaryEntry mEntry;
	public EquipmentDisplay(Soul soul, String title, Player player, BestiaryEntry entry) {
		super(player, 36,  BestiaryUtils.hashColor(title) + soul.getPlaceholder().getItemMeta().getDisplayName() + "'s Equipment");
		mSoul = soul;
		mEntry = entry;
		loadWindow(mSoul);
	}

	private void loadWindow(Soul soul) {
		//Gotta grab everything first
		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());
		ItemsVariable itemsVar = new ItemsVariable("ArmorItems", new String[] {"Feet Equipment", "Legs Equipment", "Chest Equipment", "Head Equipment"});
		ItemsVariable handVar = new ItemsVariable("HandItems", new String[] {"Offhand", "Mainhand"});
		ItemStack[] armorItems = ((ItemsVariable)itemsVar.bind(entityNBT.getData())).getItems();
		ItemStack[] handItems = ((ItemsVariable)handVar.bind(entityNBT.getData())).getItems();

		ItemStack filler = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, filler);
		}

		ItemStack nullItem = new ItemStack(Material.BARRIER);
		ItemMeta meta = nullItem.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Nothing here");
		nullItem.setItemMeta(meta);

		//Using i just because it makes things simpler
		for (int i = 0; i < 4; i++) {
			ItemStack armorItem = armorItems[i];
			if (armorItem == null || armorItem.getType() == Material.AIR) {
				_inventory.setItem(13 - i, nullItem);
				continue;
			}

			_inventory.setItem(13 - i, armorItem);
		}

		for (int i = 0; i < 2; i++) {
			ItemStack handItem = handItems[i];
			if (handItem == null || handItem.getType() == Material.AIR) {
				_inventory.setItem(15 + i, nullItem);
				continue;
			}

			_inventory.setItem(15 + i, handItem);
		}

		ItemStack goBackItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		meta = goBackItem.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Go Back");
		goBackItem.setItemMeta(meta);

		_inventory.setItem(31, goBackItem);
	}

	@Override
	public void inventoryClick(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		Player player = (Player)event.getWhoClicked();
		if (slot == 31 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
			mEntry.clone().openInventory(player, LibraryOfSouls.getInstance());
		}
		event.setCancelled(true);
	}
}
