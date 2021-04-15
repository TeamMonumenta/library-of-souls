package com.playmonumenta.libraryofsouls.bestiary;

import java.util.List;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;

import org.bukkit.ChatColor;
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

public class BestiaryAreaInventory extends CustomInventory {
	public static final Material EMPTY_MAT = Material.BLUE_STAINED_GLASS_PANE;
	public static final Material GO_BACK_MAT = Material.RED_STAINED_GLASS_PANE;
	public static final Material CHANGE_PAGE_MAT = Material.GREEN_STAINED_GLASS_PANE;

	public static final ItemStack GO_BACK_ITEM = new ItemStack(GO_BACK_MAT);

	static {
		ItemMeta meta = GO_BACK_ITEM.getItemMeta();
		meta.displayName(Component.text("Go Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
		GO_BACK_ITEM.setItemMeta(meta);
	}

	private final int mOffset;
	private final BestiaryArea mArea;
	private final List<BestiaryEntryInterface> mChildren;

	public BestiaryAreaInventory(Player player, BestiaryArea area, int offset) {
		super(player, 36, ChatColor.BLACK + "Bestiary: " + LegacyComponentSerializer.legacySection().serialize(area.getName()));
		mOffset = offset;
		mArea = area;
		mChildren = mArea.getBestiaryChildren();

		for (int i = 0 + mOffset; i < 27 + mOffset; i++) {
			if (i < mChildren.size()) {
				_inventory.setItem(i - mOffset, mChildren.get(i).getBestiaryItem(player));
			} else {
				_inventory.setItem(i - mOffset, new ItemStack(EMPTY_MAT));
			}
		}

		for (int i = 27; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(EMPTY_MAT));
		}

        if (mOffset > 0) {
			_inventory.setItem(27, UtilsMc.newSingleItemStack(CHANGE_PAGE_MAT, "[" + Integer.toString(mOffset / 27) + "] Previous Page"));
		}

		if (27 + mOffset < mChildren.size()) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(CHANGE_PAGE_MAT, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
		}

		if (area.getBestiaryParent() != null) {
			_inventory.setItem(31, GO_BACK_ITEM);
		}
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {
		/* Always cancel the event */
		event.setCancelled(true);

		/* Ignore non-left clicks */
		if (!event.getClick().equals(ClickType.LEFT)) {
			return;
		}

		Player player = (Player)event.getWhoClicked();
		int slot = event.getRawSlot();

		if (slot >= 0 && slot < 27 && slot + mOffset < mChildren.size()) {
			/* Clicked a valid entry */
			BestiaryEntryInterface clickedEntry = mChildren.get(slot + mOffset);
			if (clickedEntry.canOpenBestiary(player)) {
				clickedEntry.openBestiary(player, mArea);
			}
		} else if (slot == 27 && event.getCurrentItem().getType().equals(CHANGE_PAGE_MAT)) {
			/* Previous Page */
			new BestiaryAreaInventory(player, mArea, mOffset - 27).openInventory(player, LibraryOfSouls.getInstance());
		} else if (slot == 31 && event.getCurrentItem().getType().equals(GO_BACK_MAT)) {
			/* Go Back
			 * Note that parent's parent is passed as null here - must rely on the class to figure out its own parent
			 * That information isn't practical to determine here
			 */
			mArea.getBestiaryParent().openBestiary(player, null);
		} else if (slot == 35 && event.getCurrentItem().getType().equals(CHANGE_PAGE_MAT)) {
			/* Next Page */
			new BestiaryAreaInventory(player, mArea, mOffset + 27).openInventory(player, LibraryOfSouls.getInstance());
		}
	}
}
