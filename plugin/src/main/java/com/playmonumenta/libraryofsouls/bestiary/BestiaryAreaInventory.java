package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public class BestiaryAreaInventory extends CustomInventory {
	public static final Material EMPTY_MAT = Material.BLUE_STAINED_GLASS_PANE;
	public static final Material GO_BACK_MAT = Material.RED_STAINED_GLASS_PANE;
	public static final Material CHANGE_PAGE_MAT = Material.GREEN_STAINED_GLASS_PANE;
	public static final Material CHANGE_ENTRY_MAT = Material.LIME_STAINED_GLASS_PANE;

	public static final ItemStack GO_BACK_ITEM = new ItemStack(GO_BACK_MAT);
	public static final ItemStack MOVE_ENTRY_PREV_ITEM = new ItemStack(CHANGE_ENTRY_MAT);
	public static final ItemStack MOVE_ENTRY_NEXT_ITEM = new ItemStack(CHANGE_ENTRY_MAT);
	public static final ItemStack EMPTY_ITEM = new ItemStack(EMPTY_MAT);

	static {
		ItemMeta meta = GO_BACK_ITEM.getItemMeta();
		meta.displayName(Component.text("Go Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
		GO_BACK_ITEM.setItemMeta(meta);

		meta = MOVE_ENTRY_PREV_ITEM.getItemMeta();
		meta.displayName(Component.text("Previous Entry", NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false));
		MOVE_ENTRY_PREV_ITEM.setItemMeta(meta);

		meta = MOVE_ENTRY_NEXT_ITEM.getItemMeta();
		meta.displayName(Component.text("Next Entry", NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false));
		MOVE_ENTRY_NEXT_ITEM.setItemMeta(meta);

		meta = EMPTY_ITEM.getItemMeta();
		meta.displayName(Component.text("", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
		EMPTY_ITEM.setItemMeta(meta);
	}

	private final int mOffset;
	private final BestiaryArea mArea;
	private final List<BestiaryEntryInterface> mChildren;

	public BestiaryAreaInventory(Player player, BestiaryArea area, int offset) {
		super(player, 54, ChatColor.BLACK + "Bestiary: " + PlainComponentSerializer.plain().serialize(area.getName()));
		mOffset = offset;
		mArea = area;
		mChildren = mArea.getBestiaryChildren();

		for (int i = 0; i < 54; i++) {
			_inventory.setItem(i, EMPTY_ITEM);
		}

		int offsetOffset = 10;
		for (int i = 10 + mOffset; i < 46 + mOffset; i++) {
			if ((i - mOffset) % 9 == 0 || (i - mOffset) % 9 == 8) {
				offsetOffset++;
				continue;
			}

			if (i < mChildren.size() + offsetOffset) {
				BestiaryEntryInterface entry = mChildren.get(i - offsetOffset);
				ItemStack item = entry.getBestiaryItem(player).clone();
				if (entry instanceof BestiaryArea) {
					BestiaryArea progressArea = (BestiaryArea)entry;
					double total = getTotal(progressArea, player);
					double discovered = totalDiscovered(progressArea, player);
					int bars = (int)Math.floor((discovered / total) * 20);

					if (bars < 0) {
						continue;
					}

					Component progressBar = Component.text("[", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);

					String firstBars = "|".repeat(bars);
					progressBar = progressBar.append(Component.text(firstBars, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));

					String secondBars = "|".repeat(20 - bars);
					progressBar = progressBar.append(Component.text(secondBars, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

					progressBar = progressBar.append(Component.text("]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
					progressBar = progressBar.append(Component.text(" " + discovered + "/" + total, NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
					ItemMeta meta = item.getItemMeta();
					List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
					if (!lore.contains(progressBar)) {
						lore.add(progressBar);
						meta.lore(lore);
						item.setItemMeta(meta);
					}
				}
				_inventory.setItem(i - mOffset, item);
			}
		}

		if (mOffset > 0) {
			ItemStack item = new ItemStack(CHANGE_PAGE_MAT);
			ItemMeta meta = item.getItemMeta();
			meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
			item.setItemMeta(meta);
			_inventory.setItem(45, item);
		}

		if (27 + mOffset < mChildren.size()) {
			ItemStack item = new ItemStack(CHANGE_PAGE_MAT);
			ItemMeta meta = item.getItemMeta();
			meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
			item.setItemMeta(meta);
			_inventory.setItem(53, item);
		}

		if (area.getBestiaryParent() != null) {
			_inventory.setItem(49, GO_BACK_ITEM);
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
		int indexSlot = accountForOffset(slot, (Player)event.getWhoClicked());
		if (indexSlot >= 0 && indexSlot < 29 && indexSlot + mOffset < mChildren.size()) {
			/* Clicked a valid entry */
			BestiaryEntryInterface clickedEntry = mChildren.get(indexSlot + mOffset);
			if (clickedEntry.canOpenBestiary(player)) {
				clickedEntry.openBestiary(player, mArea, mChildren, indexSlot + mOffset);
			}
		} else if (slot == 45 && event.getCurrentItem().getType().equals(CHANGE_PAGE_MAT)) {
			/* Previous Page */
			new BestiaryAreaInventory(player, mArea, mOffset - 28).openInventory(player, LibraryOfSouls.getInstance());
		} else if (slot == 49 && event.getCurrentItem().getType().equals(GO_BACK_MAT)) {
			/* Go Back
			 * Note that parent's parent is passed as null here - must rely on the class to figure out its own parent
			 * That information isn't practical to determine here
			 */
			mArea.getBestiaryParent().openBestiary(player, null, null, -1);
		} else if (slot == 53 && event.getCurrentItem().getType().equals(CHANGE_PAGE_MAT)) {
			/* Next Page */
			new BestiaryAreaInventory(player, mArea, mOffset + 28).openInventory(player, LibraryOfSouls.getInstance());
		}
	}

	public int accountForOffset(int slot, Player player) {
		slot = slot - 10;
		if (slot > 9 && slot < 18) {
			return slot - 2;
		} else if (slot > 18 && slot < 27) {
			return slot - 4;
		} else if (slot >= 27) {
			return slot - 6;
		} else {
			return slot;
		}
	}

	public double totalDiscovered(BestiaryEntryInterface entry, Player player) {
		if (entry instanceof SoulEntry) {
			SoulEntry soul = (SoulEntry)entry;
			double kills = BestiaryManager.getKillsForMob(player, soul);
			if (kills >= 1) {
				return 1;
			} else {
				return 0;
			}
		} else if (entry instanceof BestiaryArea) {
			BestiaryArea area = (BestiaryArea)entry;
			double discovered = 0;
			List<BestiaryEntryInterface> children = area.getBestiaryChildren();
			for (BestiaryEntryInterface face : children) {
				discovered += totalDiscovered(face, player);
			}
			return discovered;
		} else {
			return 0;
		}
	}

	public double getTotal(BestiaryEntryInterface entry, Player player) {
		BestiaryArea area = (BestiaryArea)entry;
		List<BestiaryEntryInterface> children = area.getBestiaryChildren();
		if (children != null && children.get(0) instanceof SoulEntry) {
			return children.size();
		} else if (children != null && children.get(0) instanceof BestiaryArea) {
			double total = 0;
			for (BestiaryEntryInterface face : children) {
				total += getTotal(face, player);
			}
			return total;
		} else {
			return -1;
		}
	}
}
