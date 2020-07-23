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
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;

public class BestiaryCommand {
	public static void registerAll(String location) {
		CommandPermission perms = CommandPermission.fromString("los.getbestiary");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("location", new LiteralArgument(location));
		CommandAPI.getInstance().register("bestiary", perms, arguments, (sender, args) -> {
			Player player = (Player)sender;
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(location);
			(new BestiaryInventory(player, souls, location)).openInventory(player, LibraryOfSouls.getInstance());
		});
	}
}
