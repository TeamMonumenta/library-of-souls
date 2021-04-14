package com.playmonumenta.libraryofsouls.bestiary;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BestiaryCommand {
	private static final Function<CommandSender, String[]> LIST_LOCATIONS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[SoulsDatabase.getInstance().listMobLocations().size()]);

	public static void register() {
		final String command = "bestiary";

		new CommandAPICommand(command)
			.withSubcommand(new CommandAPICommand("get")
				.withPermission(CommandPermission.fromString("los.bestiary.get"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
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
				}))
			.withSubcommand(new CommandAPICommand("set")
				.withPermission(CommandPermission.fromString("los.bestiary.set"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					BestiaryManager.setKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				}))
			.withSubcommand(new CommandAPICommand("add")
				.withPermission(CommandPermission.fromString("los.bestiary.add"))
				.withArguments(new EntitySelectorArgument("player", EntitySelector.ONE_PLAYER))
				.withArguments(new StringArgument("mobLabel").overrideSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					int kills = 0;
					try {
						kills = BestiaryManager.addKillsToMob((Player)args[1], LibraryOfSoulsCommand.getSoul((String)args[2]), (Integer)args[3]);
					} catch (Exception ex) {
						CommandAPI.fail(ex.getMessage());
					}
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.open"))
				.executes((sender, args) -> {
					Player player = LibraryOfSoulsCommand.getPlayer(sender);
					new BestiarySelection((Player)args[0]).openInventory((Player)args[0], LibraryOfSouls.getInstance());
				}))
			.withSubcommand(new CommandAPICommand("book")
				.withPermission(CommandPermission.fromString("los.bestiary.book"))
				.executes((sender, args) -> {
					Player player = LibraryOfSoulsCommand.getPlayer(sender);
					ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
					ItemMeta meta = book.getItemMeta();
					meta.setDisplayName(ChatColor.GOLD + "Bestiary");
					List<String> lore = new ArrayList<>();
					lore.add("Written by: Erwen");
					lore.add(ChatColor.GRAY + "A compendium of every manner of beasts");
					lore.add(ChatColor.GRAY + "from across land and sea,");
					lore.add(ChatColor.GRAY + "passed from adventurer to adventurer");
					lore.add(ChatColor.GRAY + "throughout the ages.");
					World world = player.getWorld();
					Item itemEntity = world.dropItem(player.getLocation(), book);
					itemEntity.setPickupDelay(0);
				}))
			.register();
	}
}
