package com.playmonumenta.libraryofsouls.bestiary;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

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
	private static final DynamicSuggestions listMobsLocs = () -> SoulsDatabase.getInstance().listMobLocations().toArray(stringArray);
	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		CommandAPI.getInstance().register("bestiary", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			int kills = 0;
			try {
				kills = BestiaryManager.getKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]));
			} catch (Exception ex) {
				CommandAPI.fail(ex.getMessage());
			}
			if (sender instanceof Player) {
				sender.sendMessage(String.format("{}{} {}kills for mob {}{}{}: {}",
				                                 ChatColor.GREEN,
				                                 ChatColor.BLUE, ((Player)args[0]).getDisplayName(),
				                                 ChatColor.GREEN,
				                                 ChatColor.BLUE, (String)args[1],
				                                 ChatColor.GREEN,
				                                 ChatColor.BLUE,
				                                 kills));
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
		arguments.put("location", new DynamicSuggestedStringArgument(listMobsLocs));
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
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
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
	}
}
