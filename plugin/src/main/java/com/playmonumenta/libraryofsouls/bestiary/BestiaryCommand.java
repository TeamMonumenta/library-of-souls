package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BestiaryCommand {
	public static void register() {
		final String command = "bestiary";
		List<Argument> arguments = new ArrayList<>();

		arguments.add(new MultiLiteralArgument("get"));
		arguments.add(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.get"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				Soul soul = LibraryOfSoulsCommand.getSoul((String)args[2]);
				try {
					kills = BestiaryManager.getKillsForMob((Player)args[1], LibraryOfSoulsCommand.getSoul((String)args[2]));
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				if (sender instanceof Player) {
					sender.sendMessage(MessageFormat.format("{0}{1} {2}has killed {3}{4} {5}{6}",
															ChatColor.BLUE, ((Player)args[1]).getDisplayName(),
															ChatColor.WHITE,
															ChatColor.GREEN, kills,
															ChatColor.WHITE, soul.getDisplayName()));
				}
				return kills;
			})
			.register();

		arguments.clear();
		arguments.add(new MultiLiteralArgument("set"));
		arguments.add(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		arguments.add(new IntegerArgument("amount"));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.set"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				try {
					kills = BestiaryManager.setKillsForMob((Player)args[1], LibraryOfSoulsCommand.getSoul((String)args[2]), (Integer)args[3]);
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				return kills;
			})
			.register();

		arguments.clear();
		arguments.add(new MultiLiteralArgument("add"));
		arguments.add(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		arguments.add(new IntegerArgument("amount"));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.add"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				try {
					kills = BestiaryManager.addKillsToMob((Player)args[1], LibraryOfSoulsCommand.getSoul((String)args[2]), (Integer)args[3]);
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				return kills;
			})
			.register();
	}
}
