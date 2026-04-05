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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

/**
 * NbtFieldType for active_effects. Opening the GUI lets the player place potions;
 * on close, custom effects are extracted from each potion and written to the compound list.
 */
class EffectsFieldType extends NbtFieldType {

	private static final ItemStack FILLER;

	static {
		FILLER = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = FILLER.getItemMeta();
		meta.displayName(Component.empty());
		FILLER.setItemMeta(meta);
	}

	private final int mSlotCount;

	EffectsFieldType(int slotCount) {
		mSlotCount = slotCount;
	}

	@Override
	public String hint() {
		return "effects (open GUI with no value)";
	}

	@Override
	public Component formatForBook(ReadableNBT nbt, String key) {
		var list = nbt.getCompoundList(key);
		if (list.isEmpty()) {
			return Component.text("[]");
		}
		List<String> parts = new ArrayList<>();
		for (ReadWriteNBT entry : list) {
			String id = entry.getString("id");
			if (id == null) {
				continue;
			}
			String name = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
			int amp = entry.getByte("amplifier");
			int dur = entry.getInteger("duration");
			parts.add(name + (amp > 0 ? " " + (amp + 1) : "") + "(" + dur + "t)");
		}
		return Component.text(parts.isEmpty() ? "[]" : "[" + String.join(", ", parts) + "]");
	}

	@Override
	public void interact(Player player, BookOfSouls bos, String key) {
		new Inventory(player, bos, key, mSlotCount)
			.openInventory(player, LibraryOfSouls.getInstance());
	}

	@Override
	public void setFromInput(Player player, BookOfSouls bos, String key, String input)
			throws WrapperCommandSyntaxException {
		throw CommandAPI.failWithString("Use /nbos var " + key + " (no value) to open the effects editor");
	}

	private static final class Inventory extends CustomInventory {

		private final BookOfSouls mBos;
		private final Player mOwner;
		private final String mKey;
		private final int mSlotCount;

		Inventory(Player owner, BookOfSouls bos, String key, int slotCount) {
			super(owner, 9,
				Component.text(key + " — place potions", NamedTextColor.AQUA));
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
			var effectsList = entityNbt.getCompoundList(mKey);
			for (int i = 0; i < mSlotCount; i++) {
				ItemStack item = mInventory.getItem(i);
				if (item == null || item.getType() == Material.AIR) {
					continue;
				}
				ItemMeta rawMeta = item.getItemMeta();
				if (!(rawMeta instanceof PotionMeta potionMeta)) {
					mOwner.sendMessage(Component.text(
						"Slot " + (i + 1) + " is not a potion, skipped.", NamedTextColor.YELLOW));
					continue;
				}
				for (PotionEffect effect : potionMeta.getCustomEffects()) {
					ReadWriteNBT entry = effectsList.addCompound();
					entry.setString("id", effect.getType().getKey().asString());
					entry.setByte("amplifier", (byte) effect.getAmplifier());
					entry.setInteger("duration", effect.getDuration());
					entry.setByte("ambient", (byte) (effect.isAmbient() ? 1 : 0));
					entry.setByte("show_particles", (byte) (effect.hasParticles() ? 1 : 0));
					entry.setByte("show_icon", (byte) (effect.hasIcon() ? 1 : 0));
				}
			}
			mBos.saveBook();
			mOwner.getInventory().setItemInMainHand(mBos.getBook());
			mOwner.sendMessage(Component.text(mKey + " updated.", NamedTextColor.GREEN));
		}
	}
}
