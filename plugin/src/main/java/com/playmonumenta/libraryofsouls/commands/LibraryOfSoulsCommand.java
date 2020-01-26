package com.playmonumenta.libraryofsouls.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.goncalomb.bukkit.mylib.command.MyCommand;
import com.goncalomb.bukkit.mylib.command.MyCommandException;
import com.goncalomb.bukkit.mylib.utils.Utils;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.SoulsInventory;

public class LibraryOfSoulsCommand extends MyCommand {
	public LibraryOfSoulsCommand() {
		super("libraryofsouls", "los");
	}

	@Command(args = "open", type = CommandType.PLAYER_ONLY)
	public boolean openCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		(new SoulsInventory(player, SoulsDatabase.getInstance().getSouls(), "")).openInventory(player, LibraryOfSouls.getInstance());
		return true;
	}

	// TODO: This should be removed once real adding support is present
	@Command(args = "save", type = CommandType.PLAYER_ONLY)
	public boolean saveCommand(CommandSender sender, String[] args) throws MyCommandException {
		SoulsDatabase.getInstance().save();
		return true;
	}

	@Command(args = "get", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<name>")
	public boolean getCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		if (args.length != 1) {
			return false;
		}

		ItemStack bos = SoulsDatabase.getInstance().getSoul(args[0]).getBoS();
		if (bos != null) {
			CommandUtils.giveItem(player, bos);
		}

		return true;
	}

	@TabComplete(args = "get")
	public List<String> getTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobNames(), args.length >= 1 ? args[0] : null);
	}

	@Command(args = "del", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<name>")
	public boolean delCommand(CommandSender sender, String[] args) throws MyCommandException {
		if (args.length != 1) {
			return false;
		}

		return SoulsDatabase.getInstance().del(sender, args[0]);
	}

	@TabComplete(args = "del")
	public List<String> delTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobNames(), args.length >= 1 ? args[0] : null);
	}

	@Command(args = "search", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<location>")
	public boolean searchCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		if (args.length != 1) {
			return false;
		}

		List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(args[0]);
		if (souls == null) {
			return false;
		}

		(new SoulsInventory(player, souls, args[0])).openInventory(player, LibraryOfSouls.getInstance());
		return true;
	}

	@TabComplete(args = "search")
	public List<String> searchTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobLocations(), args.length >= 1 ? args[0] : null);
	}
}
