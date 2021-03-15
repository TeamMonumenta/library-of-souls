package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BestiaryCommand {
	private static final Function<CommandSender, String[]> LIST_LOCATIONS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[SoulsDatabase.getInstance().listMobLocations().size()]);

	public static void register() {
		final String command = "bestiary";

		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.get"))
			.withArguments(new MultiLiteralArgument("get"))
			.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
			.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
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

		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.set"))
			.withArguments(new MultiLiteralArgument("set"))
			.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
			.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
			.withArguments(new IntegerArgument("amount"))
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

		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.add"))
			.withArguments(new MultiLiteralArgument("add"))
			.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
			.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
			.withArguments(new IntegerArgument("amount"))
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

		new CommandAPICommand(command)
			.withPermission(CommandPermission.fromString("los.bestiary.open"))
			.withArguments(new StringArgument("location").overrideSuggestions(LIST_LOCATIONS_FUNCTION))
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

		new CommandAPICommand(("bestiaryopen"))
			.withPermission(CommandPermission.fromString("los.bestiary.open"))
			.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
			.executes((sender, args) -> {
				new BestiarySelection((Player)args[0]).openInventory((Player)args[0], LibraryOfSouls.getInstance());
			})
			.register();
	}
}
