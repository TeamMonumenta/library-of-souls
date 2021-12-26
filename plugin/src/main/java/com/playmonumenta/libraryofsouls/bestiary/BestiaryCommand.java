package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BestiaryCommand {
	public static void register() {
		final String command = "bestiary";

		new CommandAPICommand(command)
			.withSubcommand(new CommandAPICommand("get")
				.withPermission(CommandPermission.fromString("los.bestiary.get"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
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
																ChatColor.BLUE, ((Player)args[0]).getName(),
																ChatColor.WHITE,
																ChatColor.GREEN, kills,
																ChatColor.WHITE, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName())));
					}
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("set")
				.withPermission(CommandPermission.fromString("los.bestiary.set"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					BestiaryManager.setKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				}))
			.withSubcommand(new CommandAPICommand("add")
				.withPermission(CommandPermission.fromString("los.bestiary.add"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					int kills = 0;
					try {
						kills = BestiaryManager.addKillsToMob((Player)args[1], LibraryOfSoulsCommand.getSoul((String)args[2]), (Integer)args[3]);
					} catch (Exception ex) {
						CommandAPI.fail(ex.getMessage());
					}
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.open"))
				.executes((sender, args) -> {
					Player player = LibraryOfSoulsCommand.getPlayer(sender);
					BestiaryArea bestiary = LibraryOfSouls.Config.getBestiary();
					if (bestiary == null) {
						player.sendMessage(ChatColor.RED + "Bestiary not loaded");
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.openother"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.executes((sender, args) -> {
					Player player = (Player)args[0];
					BestiaryArea bestiary = LibraryOfSouls.Config.getBestiary();
					if (bestiary == null) {
						player.sendMessage(ChatColor.RED + "Bestiary not loaded");
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("lore")
					.withPermission(CommandPermission.fromString("los.bestiary.lore"))
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.withArguments(new TextArgument("lore"))
					.executes((sender, args) -> {
						if (!(sender instanceof Player)) {
							sender.sendMessage("Gotta do this as a player until I remember how to make this work in command blocks. If you need that much for lore tell me.");
						}
						SoulsDatabase.getInstance().getSoul((String)args[0]).setLore((String)args[1], (Player)sender);
					}))
			.withSubcommand(new CommandAPICommand("lore")
				.withSubcommand(new CommandAPICommand("clear")
				.withPermission(CommandPermission.fromString("los.bestiary.lore"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executes((sender, args) -> {
					if (!(sender instanceof Player)) {
						sender.sendMessage("Gotta do this as a player until I remember how to make this work in command blocks. If you need that much for lore tell me.");
					}
					SoulsDatabase.getInstance().getSoul((String)args[0]).setLore("", (Player)sender);
				})))
			.register();
	}
}
