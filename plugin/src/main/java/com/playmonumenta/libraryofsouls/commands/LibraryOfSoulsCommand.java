package com.playmonumenta.libraryofsouls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsInventory;

public class LibraryOfSoulsCommand implements CommandExecutor {
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

		(new SoulsInventory(player)).openInventory(player, LibraryOfSouls.getInstance());

		return true;
	}
}
