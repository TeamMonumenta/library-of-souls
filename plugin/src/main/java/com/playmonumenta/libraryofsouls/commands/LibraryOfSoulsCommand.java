package com.playmonumenta.libraryofsouls.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.command.MyCommand;
import com.goncalomb.bukkit.mylib.command.MyCommandException;
import com.goncalomb.bukkit.mylib.utils.Utils;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
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

	@Command(args = "get", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<name>")
	public boolean getCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		if (args.length != 1) {
			return false;
		}

		ItemStack bos = SoulsDatabase.getInstance().getBoS(args[0]);
		if (bos != null) {
			CommandUtils.giveItem(player, bos);
		}

		return true;
	}

	@TabComplete(args = "get")
	public List<String> openTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobNames(), args.length >= 1 ? args[0] : null);
	}
}
