package com.playmonumenta.libraryofsouls.bestiary;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;

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
	private final List<BestiaryEntryInterface> mSoulPeers;
	private final int mSoulPeerIndex;
	private int mPrevEntry = -1;
	private int mNextEntry = 40000;

	public BestiarySoulEquipmentInventory(Player player, SoulEntry soul, BestiaryArea soulsParent, List<BestiaryEntryInterface> soulPeers, int soulPeerIndex) {
		super(player, 36, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName()) + "'s Equipment");
		mSoul = soul;
		mSoulsParent = soulsParent;
		mSoulPeers = soulPeers;
		mSoulPeerIndex = soulPeerIndex;

		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());
		ItemsVariable itemsVar = new ItemsVariable("ArmorItems", new String[] {"Feet Equipment", "Legs Equipment", "Chest Equipment", "Head Equipment"});
		ItemsVariable handVar = new ItemsVariable("HandItems", new String[] {"Offhand", "Mainhand"});
		ItemStack[] armorItems = ((ItemsVariable)itemsVar.bind(entityNBT.getData())).getItems();
		ItemStack[] handItems = ((ItemsVariable)handVar.bind(entityNBT.getData())).getItems();

		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, BestiaryAreaInventory.EMPTY_ITEM);
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

		for (int i = mSoulPeerIndex - 1; i >= 0; i--) {
			if (mSoulPeers.get(i).canOpenBestiary(player) && ((SoulEntry)mSoulPeers.get(i)).getInfoTier(player) == SoulEntry.InfoTier.EVERYTHING && i >= 0) {
				mPrevEntry = i;
				break;
			}
		}

		for (int i = mSoulPeerIndex + 1; i < mSoulPeers.size(); i++) {
			if (mSoulPeers.get(i).canOpenBestiary(player) && ((SoulEntry)mSoulPeers.get(i)).getInfoTier(player) == SoulEntry.InfoTier.EVERYTHING && i < mSoulPeers.size()) {
				mNextEntry = i;
				break;
			}
		}

		if (mPrevEntry >= 0) {
			_inventory.setItem(27, BestiaryAreaInventory.MOVE_ENTRY_PREV_ITEM);
		}

		if (mNextEntry < mSoulPeers.size()) {
			_inventory.setItem(35, BestiaryAreaInventory.MOVE_ENTRY_NEXT_ITEM);
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
			mSoul.openBestiary((Player)event.getWhoClicked(), mSoulsParent, mSoulPeers, mSoulPeerIndex);
		} else if (event.getRawSlot() == 27 && mPrevEntry >= 0 && event.getCurrentItem().getType().equals(BestiaryAreaInventory.CHANGE_ENTRY_MAT) && mSoulPeers.get(mPrevEntry).canOpenBestiary((Player)event.getWhoClicked())) {
			new BestiarySoulEquipmentInventory((Player)event.getWhoClicked(), (SoulEntry)mSoulPeers.get(mPrevEntry), mSoulsParent, mSoulPeers, mPrevEntry).openInventory((Player)event.getWhoClicked(), LibraryOfSouls.getInstance());
		} else if (event.getRawSlot() == 35 && mNextEntry < mSoulPeers.size() && event.getCurrentItem().getType().equals(BestiaryAreaInventory.CHANGE_ENTRY_MAT) && mSoulPeers.get(mNextEntry).canOpenBestiary((Player)event.getWhoClicked())) {
			new BestiarySoulEquipmentInventory((Player)event.getWhoClicked(), (SoulEntry)mSoulPeers.get(mNextEntry), mSoulsParent, mSoulPeers, mNextEntry).openInventory((Player)event.getWhoClicked(), LibraryOfSouls.getInstance());
		}
	}

	public BestiaryArea getParent() {
		return mSoulsParent;
	}
}
