package com.playmonumenta.libraryofsouls.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;

public class LibraryOfSoulsCommand implements CommandExecutor {
	final static class LibraryOfSoulsInventory extends CustomInventory {
		public LibraryOfSoulsInventory(Player owner) {
			super(owner, 54, "Books of Souls Test 2");

			String nbtString = "{id:\"minecraft:zombie\",CustomName:\"{\\\"text\\\":\\\"§6Corrupt Foreman\\\"}\",Health:30.0f,ArmorItems:[{id:\"minecraft:leather_boots\",tag:{display:{color:4210768}},Count:1b},{id:\"minecraft:chainmail_leggings\",tag:{},Count:1b},{id:\"minecraft:leather_chestplate\",tag:{display:{Lore:[\"$$$\"],color:4210768}},Count:1b},{id:\"minecraft:player_head\",tag:{SkullOwner:{Id:\"3eed254a-9f4a-4dd1-8f9c-91a93a448f5e\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDY1YmIxOTJhNjU2YmQ2ZGVjOGQ4YzhlNGRiM2I1NzNjZjcxNjliYjczOTM5MTZlZjlhYzE5ZGExNjNhZGE1In19fQ==\"}]}},display:{Name:\"{\\\"text\\\":\\\"Miner\\\"}\"}},Count:1b}],Attributes:[{Base:30.0d,Name:\"generic.maxHealth\"}],Tags:[\"Elite\"],HandItems:[{id:\"minecraft:stone_pickaxe\",tag:{Enchantments:[{lvl:2s,id:\"minecraft:sharpness\"}]},Count:1b},{id:\"minecraft:torch\",Count:8b}],CustomNameVisible:0b}";

			final NBTTagCompound compound = NBTTagCompound.fromString(nbtString);

			_inventory.addItem((new BookOfSouls(EntityNBT.fromEntityData(compound))).getBook());
		}

		@Override
		protected void inventoryClick(final InventoryClickEvent event) {
			final int slot = event.getRawSlot();
			if (slot >= 0 && slot < 54) {
				final ItemStack item = event.getCurrentItem().clone();
				if (event.isShiftClick()) {
					Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
						@Override
						public void run() {
							event.setCurrentItem(item);
						}
					});
				} else {
					if (event.getCursor().getType() == Material.AIR) {
						event.getView().setCursor(item);
					}
					event.setCancelled(true);
				}
			} else if (event.getCursor().getType() == Material.AIR) {
				event.setCancelled(true);
			}
		}

		@Override
		protected void inventoryClose(InventoryCloseEvent event) { }
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) {
		// This command can be run by players at any time by typing /questtrigger or clicking
		// a chat message, potentially one that is old higher up in the chat.
		//
		// Therefore we must keep the state / arguments separate from the command itself, and
		// only use the command to know that one of the available dialog actions has been
		// chosen.

		// The player must be the CommandSender when they either type in /questtrigger or
		// click a dialog option in chat
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be run by players");
			return false;
		}

		Player player = (Player)sender;

		(new LibraryOfSoulsInventory(player)).openInventory(player, LibraryOfSouls.getInstance());

		return true;
	}
}
