package com.playmonumenta.libraryofsouls.bestiary;

import java.util.LinkedHashMap;

import org.bukkit.entity.Player;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;

public class GetBestiaryCommand {
	public static void register() {
		CommandPermission perms = CommandPermission.NONE;
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		String[] aliases = {"getb"};
		CommandAPI.getInstance().register("getbestiary", perms, aliases, arguments, (sender, args) -> {
			if (true) {
				GetBestiary.getBook((Player)sender);
				return;
			}
//			((Player)sender).sendMessage(ChatColor.DARK_RED + "You have not gained the ability to access the bestiary from beyond the veil!" + ChatColor.BOLD);
		});
	}
}
