package com.playmonumenta.libraryofsouls.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.goncalomb.bukkit.mylib.command.MyCommand;
import com.goncalomb.bukkit.mylib.command.MyCommandException;
import com.goncalomb.bukkit.mylib.utils.Utils;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
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

		if (args.length != 0) {
			return false;
		}

		(new SoulsInventory(player, SoulsDatabase.getInstance().getSouls(), "")).openInventory(player, LibraryOfSouls.getInstance());
		return true;
	}

	@Command(args = "get", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<name>")
	public boolean getCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		if (args.length != 1) {
			return false;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(args[0]);
		if (soul == null) {
			return false;
		}

		CommandUtils.giveItem(player, soul.getBoS());

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

		SoulsDatabase.getInstance().del(sender, args[0]);
		return true;
	}

	@TabComplete(args = "del")
	public List<String> delTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobNames(), args.length >= 1 ? args[0] : null);
	}

	@Command(args = "add", type = CommandType.PLAYER_ONLY)
	public boolean addCommand(CommandSender sender, String[] args) throws MyCommandException {
		if (args.length != 0 || !(sender instanceof Player)) {
			return false;
		}

		BookOfSouls bos = com.playmonumenta.libraryofsouls.utils.Utils.getBos((Player) sender, true);
		if (bos == null) {
			sender.sendMessage(ChatColor.RED + "You must be holding a Book of Souls");
			return true;
		}

		SoulsDatabase.getInstance().add((Player)sender, bos);
		return true;
	}

	@Command(args = "update", type = CommandType.PLAYER_ONLY)
	public boolean updateCommand(CommandSender sender, String[] args) throws MyCommandException {
		if (args.length != 0 || !(sender instanceof Player)) {
			return false;
		}

		BookOfSouls bos = com.playmonumenta.libraryofsouls.utils.Utils.getBos((Player) sender, true);
		if (bos == null) {
			sender.sendMessage(ChatColor.RED + "You must be holding a Book of Souls");
			return true;
		}

		SoulsDatabase.getInstance().update((Player)sender, bos);
		return true;
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

	@Command(args = "history", type = CommandType.PLAYER_ONLY, minargs = 1, usage = "<location>")
	public boolean historyCommand(CommandSender sender, String[] args) throws MyCommandException {
		Player player = (Player)sender;

		if (args.length != 1) {
			return false;
		}

		SoulEntry soul = SoulsDatabase.getInstance().getSoul(args[0]);
		if (soul == null) {
			return false;
		}

		(new SoulsInventory(player, soul.getHistory(), "History")).openInventory(player, LibraryOfSouls.getInstance());
		return true;
	}

	@TabComplete(args = "history")
	public List<String> historyTabComplete(CommandSender sender, String[] args) {
		return Utils.getElementsWithPrefix(SoulsDatabase.getInstance().listMobNames(), args.length >= 1 ? args[0] : null);
	}
}
