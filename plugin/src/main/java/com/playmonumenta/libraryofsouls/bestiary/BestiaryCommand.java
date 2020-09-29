package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.LinkedHashMap;

import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

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

public class BestiaryCommand {
	private static final String stringArray[] = new String[1];
	private static final DynamicSuggestions listMobs = () -> SoulsDatabase.getInstance().listMobNames().toArray(stringArray);
	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
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
		});

		arguments.clear();
		arguments.put("set", new LiteralArgument("set"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("amount", new IntegerArgument());
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			int kills = 0;
			try {
				kills = BestiaryManager.setKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
			} catch (Exception ex) {
				CommandAPI.fail(ex.getMessage());
			}
			return kills;
		});

		arguments.clear();
		arguments.put("add", new LiteralArgument("add"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
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
	}
}
