package com.playmonumenta.libraryofsouls.bestiary;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.playmonumenta.libraryofsouls.SoulEntry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BestiarySoulEquipmentInventory extends CustomInventory {
	private static final ItemStack NULL_ITEM = new ItemStack(Material.BARRIER);

	static {
		ItemMeta meta = NULL_ITEM.getItemMeta();
		meta.displayName(Component.text("Nothing here", NamedTextColor.RED, TextDecoration.ITALIC));
		NULL_ITEM.setItemMeta(meta);
	}

	private final SoulEntry mSoul;
	private final BestiaryArea mSoulsParent;

	public BestiarySoulEquipmentInventory(Player player, SoulEntry soul, BestiaryArea soulsParent) {
		super(player, 36, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName()) + "'s Equipment");
		mSoul = soul;
		mSoulsParent = soulsParent;

		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());
		ItemsVariable itemsVar = new ItemsVariable("ArmorItems", new String[] {"Feet Equipment", "Legs Equipment", "Chest Equipment", "Head Equipment"});
		ItemsVariable handVar = new ItemsVariable("HandItems", new String[] {"Offhand", "Mainhand"});
		ItemStack[] armorItems = ((ItemsVariable)itemsVar.bind(entityNBT.getData())).getItems();
		ItemStack[] handItems = ((ItemsVariable)handVar.bind(entityNBT.getData())).getItems();

		ItemStack filler = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, filler);
		}

		for (int i = 0; i < 4; i++) {
			ItemStack armorItem = armorItems[i];
			if (armorItem == null || armorItem.getType() == Material.AIR) {
				_inventory.setItem(13 - i, NULL_ITEM);
				continue;
			}

			_inventory.setItem(13 - i, armorItem);
		}

		for (int i = 0; i < 2; i++) {
			ItemStack handItem = handItems[i];
			if (handItem == null || handItem.getType() == Material.AIR) {
				_inventory.setItem(15 + i, NULL_ITEM);
				continue;
			}

			_inventory.setItem(15 + i, handItem);
		}

		_inventory.setItem(31, BestiaryAreaInventory.GO_BACK_ITEM);
	}

	@Override
	public void inventoryClick(InventoryClickEvent event) {
		/* Always cancel the event */
		event.setCancelled(true);

		/* Ignore non-left clicks */
		if (!event.getClick().equals(ClickType.LEFT)) {
			return;
		}

		if (event.getRawSlot() == 31 && event.getCurrentItem().getType().equals(BestiaryAreaInventory.GO_BACK_MAT)) {
			/* Go Back */
			mSoul.openBestiary((Player)event.getWhoClicked(), mSoulsParent);
		}
	}
}
