package com.playmonumenta.libraryofsouls;

import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.playmonumenta.libraryofsouls.utils.Utils;

public class SpawnerInventory extends CustomInventory{

	public SpawnerInventory(Player owner, String mobName, ItemStack spawner) {
		super(owner, 27, Utils.hashColor(mobName));

		loadWindow(spawner, owner, mobName);
	}

	private void loadWindow(ItemStack spawnerItem, Player owner, String mobName) {
		BlockStateMeta meta = (BlockStateMeta)spawnerItem.getItemMeta();
		BlockState state = meta.getBlockState();
		if (!(state instanceof CreatureSpawner)) {
			owner.sendMessage("This item is not a spawner... somehow");
			return;
		}
		spawnerItem = changeActivationRange(spawnerItem, 10, mobName);
		_inventory.setItem(11, spawnerItem);

		spawnerItem = changeActivationRange(spawnerItem, 12, mobName);
		_inventory.setItem(13, spawnerItem);

		spawnerItem = changeActivationRange(spawnerItem, 16, mobName);
		_inventory.setItem(15, spawnerItem);
	}

	private ItemStack changeActivationRange(ItemStack spawnerItem, int range, String mobName) {
		BlockStateMeta spawnerMeta = (BlockStateMeta)spawnerItem.getItemMeta();
		BlockState state = spawnerMeta.getBlockState();
		CreatureSpawner spawner = (CreatureSpawner)state;
		spawner.setRequiredPlayerRange(range);
		spawnerMeta.setDisplayName(mobName + " r=" + range);
		spawnerMeta.setBlockState(spawner);
		spawnerItem.setItemMeta(spawnerMeta);
		return spawnerItem;
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {

	}
}
