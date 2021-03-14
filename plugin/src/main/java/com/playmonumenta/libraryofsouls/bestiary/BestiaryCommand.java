package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BestiaryCommand {
	private static final Function<CommandSender, String[]> LIST_LOCATIONS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[SoulsDatabase.getInstance().listMobLocations().size()]);

	public static void register() {
		final String command = "bestiary";
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.get"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				Soul soul = LibraryOfSoulsCommand.getSoul((String)args[1]);
				try {
					kills = BestiaryManager.getKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]));
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				if (sender instanceof Player) {
					sender.sendMessage(MessageFormat.format("{0}{1} {2}has killed {3}{4} {5}{6}",
															ChatColor.BLUE, ((Player)args[0]).getDisplayName(),
															ChatColor.WHITE,
															ChatColor.GREEN, kills,
															ChatColor.WHITE, soul.getDisplayName()));
				}
				return kills;
			})
			.register();

		arguments.clear();
		arguments.put("set", new LiteralArgument("set"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		arguments.put("amount", new IntegerArgument());
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.set"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				try {
					kills = BestiaryManager.setKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				return kills;
			})
			.register();

		arguments.clear();
		arguments.put("add", new LiteralArgument("add"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		arguments.put("amount", new IntegerArgument());
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiarymanager"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				try {
					kills = BestiaryManager.addKillsToMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				return kills;
			})
			.register();

		arguments.clear();
		arguments.put("location", new StringArgument().overrideSuggestions(LIST_LOCATIONS_FUNCTION));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiarymanager"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				try {
					Player player = (Player)sender;
					List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
					(new BestiaryInventory(player, souls, (String)args[0], BestiaryManager.getAllKilledMobs(player, souls), null)).openInventory(player, LibraryOfSouls.getInstance());
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
			})

			.register();

		arguments.clear();
		arguments.put("mob", new StringArgument().overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiarymanager"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				try {
					Player player = (Player)sender;
					List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
					(new BestiaryInventory(player, souls, (String)args[0], BestiaryManager.getAllKilledMobs(player, souls), null)).openInventory(player, LibraryOfSouls.getInstance());
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
			})
			.register();

		arguments.clear();
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		new CommandAPICommand(("bestiaryopen"))
			.withPermission(CommandPermission.fromString("los.bestiaryopen"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				new BestiarySelection((Player)args[0]).openInventory((Player)args[0], LibraryOfSouls.getInstance());
			})
			.register();

		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.add"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				int kills = 0;
				try {
					kills = BestiaryManager.addKillsToMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				} catch (Exception ex) {
					CommandAPI.fail(ex.getMessage());
				}
				return kills;
			})
			.register();
	}
}
