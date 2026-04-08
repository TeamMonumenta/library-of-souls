package com.playmonumenta.libraryofsouls.nbt.types;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.nbt.BookOfSouls;
import com.playmonumenta.libraryofsouls.utils.CustomInventory;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * NbtFieldType for the Passengers compound list. Opening the GUI lets the player
 * place Books of Souls; on close, each BoS's entity NBT is written to the Passengers list.
 */
class PassengersFieldType extends NbtFieldType {

	private static final ItemStack FILLER;

	static {
		FILLER = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = FILLER.getItemMeta();
		meta.displayName(Component.empty());
		FILLER.setItemMeta(meta);
	}

	private final int mSlotCount;

	PassengersFieldType(int slotCount) {
		mSlotCount = slotCount;
	}

	@Override
	public String hint() {
		return "passengers (open GUI with no value)";
	}

	@Override
	public Component formatForBook(ReadableNBT nbt, String key) {
		var list = nbt.getCompoundList(key);
		if (list.isEmpty()) {
			return Component.text("[]");
		}
		List<String> types = new ArrayList<>();
		for (ReadWriteNBT entry : list) {
			String id = entry.getString("id");
			types.add(id != null ? id.replace("minecraft:", "") : "?");
		}
		return Component.text("[" + String.join(", ", types) + "]");
	}

	@Override
	public void interact(Player player, BookOfSouls bos, String key) {
		new Inventory(player, bos, key, mSlotCount)
			.openInventory(player, LibraryOfSouls.getInstance());
	}

	@Override
	public void setFromInput(Player player, BookOfSouls bos, String key, String input)
			throws WrapperCommandSyntaxException {
		throw CommandAPI.failWithString("Use /nbos var " + key + " (no value) to open the passengers editor");
	}

	private static final class Inventory extends CustomInventory {

		private final BookOfSouls mBos;
		private final Player mOwner;
		private final String mKey;
		private final int mSlotCount;

		Inventory(Player owner, BookOfSouls bos, String key, int slotCount) {
			super(owner, 9,
				Component.text(key + " — place Books of Souls", NamedTextColor.AQUA));
			mBos = bos;
			mOwner = owner;
			mKey = key;
			mSlotCount = slotCount;

			for (int j = slotCount; j < 9; j++) {
				mInventory.setItem(j, FILLER.clone());
			}
		}

		@Override
		protected void inventoryClick(InventoryClickEvent event) {
			int slot = event.getRawSlot();
			if (slot >= mSlotCount && slot < mInventory.getSize()) {
				event.setCancelled(true);
				return;
			}
			if (event.isShiftClick() && slot >= mInventory.getSize()) {
				event.setCancelled(true);
			}
		}

		@Override
		protected void inventoryDrag(InventoryDragEvent event) {
			for (int slot : event.getRawSlots()) {
				if (slot >= mSlotCount && slot < mInventory.getSize()) {
					event.setCancelled(true);
					return;
				}
			}
		}

		@Override
		protected void inventoryClose(InventoryCloseEvent event) {
			ReadWriteNBT entityNbt = mBos.getEntityNBT();
			entityNbt.removeKey(mKey);
			var passengerList = entityNbt.getCompoundList(mKey);
			for (int i = 0; i < mSlotCount; i++) {
				ItemStack item = mInventory.getItem(i);
				if (item == null || item.getType() == Material.AIR) {
					continue;
				}
				if (!BookOfSouls.isValidBook(item)) {
					mOwner.sendMessage(Component.text(
						"Slot " + (i + 1) + " is not a Book of Souls, skipped.", NamedTextColor.YELLOW));
					continue;
				}
				ReadWriteNBT passengerNbt = BookOfSouls.bookToEntityNBT(item);
				if (passengerNbt == null) {
					mOwner.sendMessage(Component.text(
						"Slot " + (i + 1) + " has a corrupted Book of Souls, skipped.", NamedTextColor.YELLOW));
					continue;
				}
				passengerList.addCompound().mergeCompound(passengerNbt);
			}
			mBos.saveBook();
			mOwner.getInventory().setItemInMainHand(mBos.getBook());
			mOwner.sendMessage(Component.text(mKey + " updated.", NamedTextColor.GREEN));
		}
	}
}
