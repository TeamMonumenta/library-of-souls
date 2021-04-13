package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import net.md_5.bungee.api.ChatColor;

public class BestiarySelection extends CustomInventory {
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;
	private static ItemStack mNotBeaten = new ItemStack(Material.PAPER);

	static {
		ItemMeta meta = mNotBeaten.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Not Beaten!");
		mNotBeaten.setItemMeta(meta);
	}

	public BestiarySelection(Player player) {
		super(player, 36, ChatColor.BLACK + "Bestiary");
		loadWindow(player);
	}

	public void loadWindow(Player player) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		List<String> lore = new ArrayList<>();
		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
		}

		for (int i = 0; i < BestiaryUtils.mDungeonMap.size(); i++) {
			lore.clear();
			String name = BestiaryUtils.mDungeonMap.get(i);
			String obj = getScoreboard(name);
			if ((scoreboard.getObjective(obj) != null
					&& (scoreboard.getObjective(obj).getScore(player.getDisplayName()).getScore() >= 1
							|| (obj.equals("roguelike") && getRoguelikeScores(scoreboard, player)) || obj.equals("")))
					|| player.getGameMode().equals(GameMode.CREATIVE)) {
				ItemStack item = getDungeonItem(BestiaryUtils.formatWell(BestiaryUtils.mDungeonMap.get(i)));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(hashItemString(name) + BestiaryUtils.formatWell(name));
				lore.add(hashItemString(name) + BestiaryUtils.formatWell(name));
				meta.setLore(lore);
				item.setItemMeta(meta);
				_inventory.setItem(i, item);
			} else {
				_inventory.setItem(i, mNotBeaten);
			}
		}

		if (mOffset > 0) {
			_inventory.setItem(27, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE,
					"[" + Integer.toString(mOffset / 27) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
		}

		if (27 + mOffset < BestiaryUtils.mDungeonMap.size()) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE,
					"[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
		}
	}

	@Override
	public void inventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 27 && slot < BestiaryUtils.mDungeonMap.size()) {
			if (!(event.getCurrentItem().getType() == Material.PAPER)) {
				if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Region 1")
						|| event.getCurrentItem().getItemMeta().getDisplayName().contains("Region 2")) {
					new BestiaryRegionInventory(player, BestiaryUtils.mDungeonMap.get(slot)).openInventory(player,
							LibraryOfSouls.getInstance());
					event.setCancelled(true);
				} else {
					try {
						// Since this is the first level, it doesnt need to pass its information on - its whatever if you go back and its on the first page
						new BestiaryInventory(player, SoulsDatabase.getInstance().getSoulsByLocation(BestiaryUtils.mDungeonMap.get(slot)), BestiaryUtils.mDungeonMap.get(slot), BestiaryManager.getAllKilledMobs(player, SoulsDatabase.getInstance().getSoulsByLocation(BestiaryUtils.mDungeonMap.get(slot)))).openInventory(player, LibraryOfSouls.getInstance());
					} catch (Exception ex) {
						LibraryOfSouls.getInstance().getLogger().severe("Caught error in BestiaryInventory: " + ex.getMessage());
						ex.printStackTrace();
					}
					event.setCancelled(true);
				}
			}
			event.setCancelled(true);
		} else if (slot == 27 && mHasPrevPage) {
			mOffset -= 27;
			loadWindow(player);
			event.setCancelled(true);
		} else if (slot == 35 && mHasNextPage) {
			mOffset += 27;
			loadWindow(player);
			event.setCancelled(true);
		} else {
			event.setCancelled(true);
		}
	}

	private String hashItemString(String in) {
		if (BestiaryUtils.hashColor(in).equals(ChatColor.BLACK + "")) {
			return ChatColor.WHITE + "";
		}

		return BestiaryUtils.hashColor(in);
	}
}
