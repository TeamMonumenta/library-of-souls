package com.playmonumenta.libraryofsouls.bestiary;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.entity.Player;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.DynamicSuggestedStringArgument;
import io.github.jorelali.commandapi.api.arguments.DynamicSuggestedStringArgument.DynamicSuggestions;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.jorelali.commandapi.api.arguments.IntegerArgument;
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;
import io.github.jorelali.commandapi.api.arguments.StringArgument;
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
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				player.sendMessage(ChatColor.GREEN + "Kills for player " + ChatColor.BLUE + ((Player)args[0]).getDisplayName() + ChatColor.GREEN +  " for mob " + ChatColor.BLUE + (String)args[1] +
					ChatColor.GREEN + ": " + ChatColor.BLUE + BestiaryManager.getKillsForMob((Player)args[0], (String)args[1]));
			}
			return BestiaryManager.getKillsForMob((Player)args[0], (String)args[1]);
		});
		arguments.clear();
		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("objective", new StringArgument());
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			return BestiaryManager.bindKillsToEntry((Player)args[0], (String)args[1], (String)args[2]);
		});

		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("objective", new StringArgument());
		arguments.put("entry", new StringArgument());
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			return BestiaryManager.bindKillsToEntry((Player)args[0], (String)args[1], (String)args[2], (String)args[3]);
		});
		arguments.clear();

		arguments.clear();
		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("temp", new LiteralArgument("temp"));
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			return BestiaryManager.bindKillsToEntry((Player)args[0], (String)args[1], "temp");
		});

		arguments.clear();
		arguments.put("get", new LiteralArgument("get"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("temp", new LiteralArgument("temp"));
		arguments.put("entry", new StringArgument());
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			return BestiaryManager.bindKillsToEntry((Player)args[0], (String)args[1], "temp", (String)args[2]);
		});

		arguments.clear();
		arguments.put("set", new LiteralArgument("set"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("amount", new IntegerArgument());
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			Player player = (Player)args[0];
			if(!BestiaryManager.setKillsForMob((Player)args[0], (String)args[1], (Integer)args[2])) {
				player.sendMessage(ChatColor.DARK_RED + "The storage system has not been instantiated yet!");
			}
		});
		arguments.clear();

		arguments.put("add", new LiteralArgument("add"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			Player player = (Player)args[0];
			if(!BestiaryManager.addKillsToMob((Player)args[0], (String)args[1], 1)) {
				player.sendMessage(ChatColor.DARK_RED + "The storage system has not been instantiated yet!");
			}
		});
		arguments.clear();

		arguments.put("add", new LiteralArgument("add"));
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		arguments.put("amount", new IntegerArgument());
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			Player player = (Player)sender;
			if(!BestiaryManager.addKillsToMob((Player)args[0], (String)args[1], (Integer)args[2])) {
				player.sendMessage(ChatColor.DARK_RED + "The storage system has not been instantiated yet!");
			}
		});
		arguments.clear();

		arguments.put("location", new DynamicSuggestedStringArgument(listMobsLocs));
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			Player player = (Player)sender;
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
			(new BestiaryInventory(player, souls, (String)args[0])).openInventory(player, LibraryOfSouls.getInstance());
		});
		arguments.clear();

		arguments.put("mob", new DynamicSuggestedStringArgument(listMobs));
		CommandAPI.getInstance().register("bestiarymanage", CommandPermission.fromString("los.bestiarymanager"), arguments, (sender, args) -> {
			Player player = (Player)sender;
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
			(new BestiaryInventory(player, souls, (String)args[0])).openInventory(player, LibraryOfSouls.getInstance());
		});
		arguments.clear();

		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
		CommandAPI.getInstance().register("bestiaryopen", CommandPermission.fromString("los.bestiaryopen"), arguments, (sender, args) -> {
			new BestiarySelection((Player)args[0]).openInventory((Player)args[0], LibraryOfSouls.getInstance());
		});
	}
}
