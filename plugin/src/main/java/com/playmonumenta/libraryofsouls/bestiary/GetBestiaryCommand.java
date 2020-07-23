package com.playmonumenta.libraryofsouls.bestiary;

import java.util.LinkedHashMap;

import org.bukkit.entity.Player;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;

public class GetBestiaryCommand {
	public static void register() {
		CommandPermission perms = CommandPermission.fromString("monumenta.getbestiary");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		String[] aliases = {"b"};
		CommandAPI.getInstance().register("bestiary", perms, aliases, arguments, (sender, args) -> {
			GetBestiary.getBook((Player)sender);
		});
	}
}
