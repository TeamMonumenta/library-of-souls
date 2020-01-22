package com.playmonumenta.libraryofsouls.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.goncalomb.bukkit.mylib.command.MyCommand;
import com.goncalomb.bukkit.mylib.command.MyCommandException;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsInventory;

public class LibraryOfSoulsCommand extends MyCommand {
	public LibraryOfSoulsCommand() {
		super("libraryofsouls", "los");
	}

	@Command(args = "open", type = CommandType.PLAYER_ONLY)
	public boolean openCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		(new SoulsInventory(player)).openInventory(player, LibraryOfSouls.getInstance());
		return true;
	}
}
