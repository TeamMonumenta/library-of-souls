package com.playmonumenta.libraryofsouls.bestiary;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class BestiaryCommand {
	public static void register() {
		final String command = "bestiary";

		new CommandAPICommand(command)
			.withSubcommand(new CommandAPICommand("get")
				.withPermission(CommandPermission.fromString("los.bestiary.get"))
				.withArguments(new EntitySelectorArgument.OnePlayer("player"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executes((sender, args) -> {
					int kills = 0;
					Soul soul = LibraryOfSoulsCommand.getSoul((String)args[1]);
					try {
						kills = BestiaryManager.getKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]));
					} catch (Exception ex) {
						throw CommandAPI.failWithString(ex.getMessage());
					}
					if (sender instanceof Player) {
						sender.sendMessage(MessageFormat.format("{0}{1} {2}has killed {3}{4} {5}{6}",
																ChatColor.BLUE, ((Player)args[0]).getName(),
																ChatColor.WHITE,
																ChatColor.GREEN, kills,
																ChatColor.WHITE, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName())));
					}
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("set")
				.withPermission(CommandPermission.fromString("los.bestiary.set"))
				.withArguments(new EntitySelectorArgument.OnePlayer("player"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					BestiaryManager.setKillsForMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
				}))
			.withSubcommand(new CommandAPICommand("add")
				.withPermission(CommandPermission.fromString("los.bestiary.add"))
				.withArguments(new EntitySelectorArgument.OnePlayer("player"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new IntegerArgument("amount"))
				.executes((sender, args) -> {
					int kills = 0;
					try {
						kills = BestiaryManager.addKillsToMob((Player)args[0], LibraryOfSoulsCommand.getSoul((String)args[1]), (Integer)args[2]);
					} catch (Exception ex) {
						throw CommandAPI.failWithString(ex.getMessage());
					}
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.open"))
				.executes((sender, args) -> {
					Player player = LibraryOfSoulsCommand.getPlayer(sender);
					BestiaryArea bestiary = LibraryOfSouls.Config.getBestiary();
					if (bestiary == null) {
						player.sendMessage(ChatColor.RED + "Bestiary not loaded");
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.openother"))
				.withArguments(new EntitySelectorArgument.OnePlayer("player"))
				.executes((sender, args) -> {
					Player player = (Player)args[0];
					BestiaryArea bestiary = LibraryOfSouls.Config.getBestiary();
					if (bestiary == null) {
						player.sendMessage(ChatColor.RED + "Bestiary not loaded");
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("info")
				.withPermission(CommandPermission.fromString("los.bestiary.info"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executesPlayer((sender, args) -> {
					String name = (String)args[0];
					SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
					if (soul == null) {
						throw CommandAPI.failWithString("Mob '" + name + "' not found");
					} else {
						LoreTestInventory inv = new LoreTestInventory(soul, sender);
						inv.openInventory(sender);
					}
				}))
			.withSubcommand(new CommandAPICommand("lore")
				.withPermission(CommandPermission.fromString("los.bestiary.lore"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(new TextArgument("lore"))
				.executesPlayer((sender, args) -> {
					String name = (String)args[0];
					SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
					if (soul == null) {
						throw CommandAPI.failWithString("Mob '" + name + "' not found");
					} else {
						soul.setLore((String)args[1], sender);
					}
				})
				.executesProxy((sender, args) -> {
					if (sender.getCallee() instanceof Player player) {
						String name = (String)args[0];
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						} else {
							soul.setLore((String)args[1], player);
						}
					} else {
						throw CommandAPI.failWithString("Callee must be instance of Player");
					}
				}))
			.withSubcommand(new CommandAPICommand("lore")
				.withSubcommand(new CommandAPICommand("clear")
				.withPermission(CommandPermission.fromString("los.bestiary.lore"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executesPlayer((sender, args) -> {
					SoulsDatabase.getInstance().getSoul((String)args[0]).setLore("", sender);
				})))
			.register();
	}

	private static class LoreTestInventory {
		private final Inventory mInv;

		private LoreTestInventory(SoulEntry soul, Player player) {
			mInv = Bukkit.createInventory(player, 54);
			ItemStack loreItem = getLoreItem(soul);
			for (int i = 0; i < 54; i++) {
				mInv.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
			}

			mInv.setItem(33, loreItem);
		}

		private void openInventory(Player player) {
			player.openInventory(mInv);
		}

		private ItemStack getLoreItem(SoulEntry soul) {
			String lore = soul.getLore();

			ItemStack loreItem = new ItemStack(Material.BOOK);
			ItemMeta meta = loreItem.getItemMeta();
			meta.displayName(Component.text("Lore", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));

			if (lore == null || lore.equals("")) {
				List<Component> itemLore = new ArrayList<>();
				itemLore.add(Component.text("This is a bug. Or at the very least, should be.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, true));

				meta.lore(itemLore);
				loreItem.setItemMeta(meta);
				return loreItem;
			}

			List<Component> itemLore = new ArrayList<>();

			String[] loreArray = lore.split("~~~");

			for (String a : loreArray) {
				itemLore.add(Component.text(a, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, true));
			}

			meta.lore(itemLore);
			loreItem.setItemMeta(meta);

			return loreItem;
		}
	}
}
