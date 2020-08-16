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
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;

public class BestiaryCommand {
	private static final String stringArray[] = new String[1];
	private static final DynamicSuggestions listMobs = () -> SoulsDatabase.getInstance().listMobLocations().toArray(stringArray);
	public static void register() {
		CommandPermission perms = CommandPermission.fromString("los.getbestiary");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("location", new DynamicSuggestedStringArgument(listMobs));
		CommandAPI.getInstance().register("bestiary", perms, arguments, (sender, args) -> {
			Player player = (Player)sender;
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
			(new BestiaryInventory(player, souls, (String)args[0])).openInventory(player, LibraryOfSouls.getInstance());
		});
		arguments.clear();

		arguments.put("region_1", new LiteralArgument("region_1"));
		CommandAPI.getInstance().register("bestiary", perms, arguments, (sender, args) -> {
			Player player = (Player)sender;
			(new BestiaryInventory(player, "region_1")).openInventory(player, LibraryOfSouls.getInstance());
		});

		arguments.clear();
		arguments.put("region_2", new LiteralArgument("region_2"));

		CommandAPI.getInstance().register("bestiary", perms, arguments, (sender, args) -> {
			Player player = (Player)sender;
			(new BestiaryInventory(player, "region_2")).openInventory(player, LibraryOfSouls.getInstance());
		});
	}
}
