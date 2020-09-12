package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;

import net.md_5.bungee.api.ChatColor;

public class BestiaryInventory extends CustomInventory {

	private List<? extends Soul> mCurrentSlots;
	private List<? extends Soul> mSouls;
	private static String mTitle = "";
	private int mOffset;
	private int mOldOffset = 0;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;
	private Region[] mCurrentPois;
	private static ItemStack mNotFound = new ItemStack(Material.PAPER);
	private static ItemStack mGoBack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
	private Map<SoulEntry, Integer> mAvailableMobs;
	private String mPrevTitle;

	static {
		ItemMeta meta = mNotFound.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED  + "Mob not discovered!");
		mNotFound.setItemMeta(meta);

		meta = mGoBack.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Go Back");
		mGoBack.setItemMeta(meta);
	}

	//If it is not the region inventory
	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title) {
		this(owner, souls, title, BestiaryManager.getAllKilledMobs(owner, souls));
	}

	//If it is not the region Inventory
	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title, Map<SoulEntry, Integer> availableMobs) {
		this(owner, souls, title, availableMobs, null);
	}

	//So you can go back to the region inventory
	public BestiaryInventory(Player owner, List<SoulEntry> souls, Map<SoulEntry, Integer> availableMobs, String title, Region[] pois, int offset, String prevTitle) {
		this(owner, souls, title, availableMobs, pois);
		mOldOffset = offset;
		mPrevTitle = prevTitle;
	}

	//The final one
	public BestiaryInventory(Player owner, List<SoulEntry> souls, String title, Map<SoulEntry, Integer> availableMobs, Region[] pois) {
		super(owner, 36, ChatColor.BLACK + "Bestiary: " + BestiaryUtils.hashColor(title) + BestiaryUtils.formatWell(title));
		mSouls = souls;
		mTitle = title;
		mCurrentPois = pois;
		mAvailableMobs = availableMobs;
		loadWindow();
	}

	public void loadWindow() {
		mCurrentSlots = mSouls.subList(mOffset, Math.min(mSouls.size(), mOffset + 27));

		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
		}

		for (int i = 0; i < 27; i++) {
			if (i < mCurrentSlots.size()) {
				if (BestiaryUtils.getInfoTier(mCurrentSlots.get(i), mAvailableMobs) == 1) {
					ItemStack justNameItem = bestiaryPlaceholder(mCurrentSlots.get(i));
					justNameItem.setType(Material.BARRIER);
					_inventory.setItem(i, justNameItem);
				} else if (BestiaryUtils.getInfoTier(mCurrentSlots.get(i), mAvailableMobs) > 1) {
					_inventory.setItem(i, bestiaryPlaceholder(mCurrentSlots.get(i)));
				} else {
					_inventory.setItem(i, mNotFound);
				}
			}
		}

		if (mOffset > 0) {
			_inventory.setItem(27, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
		}

		if (mCurrentSlots.size() >= 27) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
		}

		_inventory.setItem(31, mGoBack);
	}

	@Override
	protected void inventoryClick (final InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 27) {
			if (event.getClick().equals(ClickType.LEFT) && mCurrentSlots != null && slot < mCurrentSlots.size()) {
					Soul soul = mCurrentSlots.get(slot);
					if (BestiaryUtils.getInfoTier(soul, mAvailableMobs) == 3) {
						new BestiaryEntry(soul, player, mTitle, slot + mOffset, mAvailableMobs, mCurrentPois, mPrevTitle, mOldOffset).openInventory(player, LibraryOfSouls.getInstance());
					} else if (BestiaryUtils.getInfoTier(soul, mAvailableMobs) == 2) {
						new BestiaryEntry(soul, player, mTitle, slot + mOffset, mAvailableMobs, mCurrentPois, mPrevTitle, mOldOffset).openInventory(player, LibraryOfSouls.getInstance());
					} else if (BestiaryUtils.getInfoTier(soul, mAvailableMobs) == 1) {
						player.sendMessage(ChatColor.DARK_RED + "You Have Not gained enough knowledge of this mob!");
					} else if (BestiaryUtils.getInfoTier(soul, mAvailableMobs) == 0) {
						event.setCancelled(true);
					}
			}
			event.setCancelled(true);
		} else if (slot == 27 && mHasPrevPage) {
			mOffset -= 27;
			loadWindow();
			event.setCancelled(true);
		} else if (slot == 31) {
			if (mCurrentPois != null) {
				new BestiaryRegionInventory(player, mPrevTitle, mCurrentPois, mOldOffset).openInventory(player, LibraryOfSouls.getInstance());
			} else {
				new BestiarySelection(player).openInventory(player, LibraryOfSouls.getInstance());
			}
			event.setCancelled(true);
		} else if (slot == 35 && mHasNextPage) {
			mOffset += 27;
			loadWindow();
			event.setCancelled(true);
		} else {
			event.setCancelled(true);
		}
	}

	private ItemStack bestiaryPlaceholder(Soul soul) {
		ItemStack bestiary = new ItemStack(soul.getPlaceholder());
		ItemMeta meta = bestiary.getItemMeta();
		List<String> lore = new ArrayList<String>();

		lore.add(hashItemString(mTitle) + BestiaryUtils.formatWell(mTitle));
		lore.add(hashItemString(mTitle) + BestiaryUtils.formatWell(EntityNBT.fromEntityData(soul.getNBT()).getEntityType().toString().toLowerCase()));
		lore.add(ChatColor.DARK_RED + "Kills: " + mAvailableMobs.get(soul));

		meta.setDisplayName(hashItemString(mTitle) + meta.getDisplayName());
		meta.setLore(lore);
		bestiary.setItemMeta(meta);
		return bestiary;
	}

	private String hashItemString(String in) {
		if (BestiaryUtils.hashColor(mTitle).equals(ChatColor.BLACK + "")) {
			return ChatColor.WHITE + "";
		}

		return BestiaryUtils.hashColor(mTitle);
	}
}
