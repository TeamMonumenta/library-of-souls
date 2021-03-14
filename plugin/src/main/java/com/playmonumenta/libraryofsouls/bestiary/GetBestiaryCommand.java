package com.playmonumenta.libraryofsouls.bestiary;

import java.util.LinkedHashMap;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;


public class GetBestiaryCommand {
	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		String[] aliases = {"getb"};
		new CommandAPICommand("getbestiary").withAliases(aliases).withArguments(arguments).executes((sender, args) -> {
			if (true) {
				GetBestiary.getBook((Player)sender);
				return;
			}
//			((Player)sender).sendMessage(ChatColor.DARK_RED + "You have not gained the ability to access the bestiary from beyond the veil!" + ChatColor.BOLD);
		});
	}
}
