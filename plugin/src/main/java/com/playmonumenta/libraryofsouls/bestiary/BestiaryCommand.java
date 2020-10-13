package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.DynamicSuggestedStringArgument;
import io.github.jorelali.commandapi.api.arguments.DynamicSuggestedStringArgument.DynamicSuggestions;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.jorelali.commandapi.api.arguments.IntegerArgument;
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;
import net.md_5.bungee.api.ChatColor;
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
	private static final DynamicSuggestions LIST_MOB_LOCS_FUNCTION = () -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[SoulsDatabase.getInstance().listMobLocations().size()]);

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
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			int kills = 0;
			try {
				kills = BestiaryManager.addKillsToMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
			} catch (Exception ex) {
				CommandAPI.fail(ex.getMessage());
			}
			return kills;
		});

		arguments.clear();
		arguments.put("location", new DynamicSuggestedStringArgument(LIST_MOB_LOCS_FUNCTION));
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			try {
				Player player = (Player)sender;
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
				(new BestiaryInventory(player, souls, (String)args[0])).openInventory(player, LibraryOfSouls.getInstance());
			} catch (Exception ex) {
				CommandAPI.fail(ex.getMessage());
			}
		});

		arguments.clear();
		arguments.put("mob", new DynamicSuggestedStringArgument(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			try {
				Player player = (Player)sender;
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
				(new BestiaryInventory(player, souls, (String)args[0])).openInventory(player, LibraryOfSouls.getInstance());
			} catch (Exception ex) {
				CommandAPI.fail(ex.getMessage());
			}
		});

		arguments.clear();
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		CommandAPI.getInstance().register("bestiaryopen", CommandPermission.fromString("los.bestiaryopen"), arguments, (sender, args) -> {
			new BestiarySelection((Player)args[0]).openInventory((Player)args[0], LibraryOfSouls.getInstance());
		});
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
