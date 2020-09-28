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

		for (int i = 0; i < BestiaryUtils.mBookMap.size(); i++) {
			lore.clear();
			String name = BestiaryUtils.mBookMap.get(i);
			String obj = getScoreboard(name);
			if ((scoreboard.getObjective(obj) != null && (scoreboard.getObjective(obj).getScore(player.getDisplayName()).getScore() >= 1
					|| (obj.equals("roguelike") && getRoguelikeScores(scoreboard, player)) || obj.equals(""))) || player.getGameMode().equals(GameMode.CREATIVE)) {
				ItemStack item = getDungeonItem(BestiaryUtils.formatWell(BestiaryUtils.mBookMap.get(i)));
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
			_inventory.setItem(27, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
		}

		if (27 + mOffset < BestiaryUtils.mBookMap.size()) {
			_inventory.setItem(35, UtilsMc.newSingleItemStack(Material.GREEN_STAINED_GLASS_PANE, "[" + Integer.toString(mOffset / 27) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
		}
	}

	@Override
	public void inventoryClick(InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 27 && slot < BestiaryUtils.mBookMap.size()) {
			if (!(event.getCurrentItem().getType() == Material.PAPER)) {
				if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Region 1") || event.getCurrentItem().getItemMeta().getDisplayName().contains("Region 2")) {
					new BestiaryRegionInventory(player, BestiaryUtils.mBookMap.get(slot)).openInventory(player, LibraryOfSouls.getInstance());
					event.setCancelled(true);
				} else {
					try {
						new BestiaryInventory(player, SoulsDatabase.getInstance().getSoulsByLocation(BestiaryUtils.mBookMap.get(slot)), BestiaryUtils.mBookMap.get(slot)).openInventory(player, LibraryOfSouls.getInstance());;
					} catch (Exception ex) {
						LibraryOfSouls.getInstance().getLogger().severe("Caught error in BestiaryInventory: " + ex.getMessage());
						ex.printStackTrace();
					}
					event.setCancelled(true);
				}
			}
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

	private String getScoreboard(String in) {
		if (in.equals("willows")) {
			return "R1Bonus";
		} else if (in.equals("sanctum")) {
			return "ForswornSanctum";
		} else if (in.equals("reverie")) {
			return "Corrupted";
		} else if (in.equals("roguelike")) {
			//have to do something extra for roguelike... 3 modes
			return in;
		} else if (in.equals("region_1")) {
			return in;
		} else if (in.equals("region_2")) {
			return "R1Complete";
		}

		return BestiaryUtils.formatWell(in).replace(" ", "");
	}

	private ItemStack getDungeonItem(String in) {
		switch (in) {
		case "White":
			return new ItemStack(Material.WHITE_WOOL);
		case "Orange":
			return new ItemStack(Material.ORANGE_WOOL);
		case "Magenta":
			return new ItemStack(Material.MAGENTA_WOOL);
		case "Light Blue":
			return new ItemStack(Material.LIGHT_BLUE_WOOL);
		case "Yellow":
			return new ItemStack(Material.YELLOW_WOOL);
		case "Lime":
			return new ItemStack(Material.LIME_WOOL);
		case "Pink":
			return new ItemStack(Material.PINK_WOOL);
		case "Gray":
			return new ItemStack(Material.GRAY_WOOL);
		case "Light Gray":
			return new ItemStack(Material.LIGHT_GRAY_WOOL);
		case "Cyan":
			return new ItemStack(Material.CYAN_WOOL);
		case "Purple":
			return new ItemStack(Material.PURPLE_WOOL);
		case "Blue":
			return new ItemStack(Material.BLUE_WOOL);
		case "Red":
			return new ItemStack(Material.RED_WOOL);
		case "Brown":
			return new ItemStack(Material.BROWN_WOOL);
		case "Green":
			return new ItemStack(Material.GREEN_WOOL);
		case "Black":
			return new ItemStack(Material.BLACK_WOOL);
		case "Labs":
			return new ItemStack(Material.IRON_BARS);
		case "Willows":
			return new ItemStack(Material.MOSSY_COBBLESTONE);
		case "Sanctum":
			return new ItemStack(Material.JUNGLE_LOG);
		case "Roguelike":
			return new ItemStack(Material.MAGMA_BLOCK);
		case "Reverie":
			return new ItemStack(Material.NETHER_WART_BLOCK);
		case "Shifting City":
			return new ItemStack(Material.PRISMARINE_BRICKS);
		case "Region 1":
			return new ItemStack(Material.GRASS_BLOCK);
		case "Region 2":
			return new ItemStack(Material.TUBE_CORAL_BLOCK);
		default:
			return new ItemStack(Material.STONE);
		}
	}

	private boolean getRoguelikeScores(Scoreboard scoreboard, Player player) {
		return scoreboard.getObjective("RogFinishedN").getScore(player.getDisplayName()).getScore() >= 1 || scoreboard.getObjective("RogFinished").getScore(player.getDisplayName()).getScore() >= 1
				|| scoreboard.getObjective("RogFinishedC").getScore(player.getDisplayName()).getScore() >= 1;
	}

	private String hashItemString(String in) {
		if (BestiaryUtils.hashColor(in).equals(ChatColor.BLACK + "")) {
			return ChatColor.WHITE + "";
		}

		return BestiaryUtils.hashColor(in);
	}
}
