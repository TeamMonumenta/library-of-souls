package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;


public class GetBestiaryCommand {
	public static void register() {
		String[] aliases = {"getb"};
		new CommandAPICommand("getbestiary")
			.withAliases(aliases)
			.executes((sender, args) -> {
				if (true) {
					GetBestiary.getBook((Player)sender);
					return;
				}
//			((Player)sender).sendMessage(ChatColor.DARK_RED + "You have not gained the ability to access the bestiary from beyond the veil!" + ChatColor.BOLD);
		});
	}
}
