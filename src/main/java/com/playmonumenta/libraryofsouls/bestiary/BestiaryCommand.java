package com.playmonumenta.libraryofsouls.bestiary;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ObjectiveArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class BestiaryCommand {
	public static void register() {
		final String command = "bestiary";

		EntitySelectorArgument.OnePlayer playerArg = new EntitySelectorArgument.OnePlayer("player");
		IntegerArgument amountArg = new IntegerArgument("amount");

		new CommandAPICommand(command)
			.withSubcommand(new CommandAPICommand("get")
				.withPermission(CommandPermission.fromString("los.bestiary.get"))
				.withArguments(playerArg)
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executes((sender, args) -> {
					int kills;
					SoulEntry soul = LibraryOfSoulsCommand.getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION)));
					Player player = args.getByArgument(playerArg);
					try {
						kills = BestiaryManager.getKillsForMob(player, soul);
					} catch (Exception ex) {
						throw CommandAPI.failWithString(ex.getMessage());
					}
					sender.sendMessage(Component.text().append(Component.text(player.getName(), NamedTextColor.BLUE))
						                   .append(Component.text(" has killed "))
						                   .append(Component.text(kills + " ", NamedTextColor.GREEN))
						                   .append(soul.getDisplayName()));
					return kills;
				}))
			.withSubcommand(new CommandAPICommand("set")
				.withPermission(CommandPermission.fromString("los.bestiary.set"))
				.withArguments(playerArg)
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(amountArg)
				.executes((sender, args) -> {
					BestiaryManager.setKillsForMob(args.getByArgument(playerArg), LibraryOfSoulsCommand.getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))), args.getByArgument(amountArg));
				}))
			.withSubcommand(new CommandAPICommand("add")
				.withPermission(CommandPermission.fromString("los.bestiary.add"))
				.withArguments(playerArg)
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(amountArg)
				.executes((sender, args) -> {
					int kills;
					try {
						kills = BestiaryManager.addKillsToMob(args.getByArgument(playerArg), LibraryOfSoulsCommand.getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))), args.getByArgument(amountArg));
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
						player.sendMessage(Component.text("Bestiary not loaded", NamedTextColor.RED));
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("open")
				.withPermission(CommandPermission.fromString("los.bestiary.openother"))
				.withArguments(playerArg)
				.executes((sender, args) -> {
					Player player = args.getByArgument(playerArg);
					BestiaryArea bestiary = LibraryOfSouls.Config.getBestiary();
					if (bestiary == null) {
						player.sendMessage(Component.text("Bestiary not loaded", NamedTextColor.RED));
					} else {
						bestiary.openBestiary(player, null, null, -1);
					}
				}))
			.withSubcommand(new CommandAPICommand("info")
				.withPermission(CommandPermission.fromString("los.bestiary.info"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.executesPlayer((sender, args) -> {
					String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
					SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
					if (soul == null) {
						throw CommandAPI.failWithString("Mob '" + name + "' not found");
					} else {
						LoreTestInventory inv = new LoreTestInventory(soul, sender);
						inv.openInventory(sender);
					}
				}))
			.withSubcommand(new CommandAPICommand("deleteall")
				.withPermission(CommandPermission.fromString("los.bestiary.deleteall"))
				.withArguments(playerArg)
				.executes((sender, args) -> {
					Player player = args.getByArgument(playerArg);
					BestiaryManager.deleteAll(player);
				}))
			.withSubcommand(new CommandAPICommand("lore")
				.withSubcommand(new CommandAPICommand("get")
					.withPermission(CommandPermission.fromString("los.bestiary.lore"))
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.executesPlayer((sender, args) -> {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						} else {
							ItemStack item = new ItemStack(Material.BOOK);
							item.lore(soul.getLore());
							Location location = sender.getLocation();
							if (!sender.getInventory().addItem(item).isEmpty()) {
								Item droppedItem = location.getWorld().dropItem(location, item);
								droppedItem.setPickupDelay(0);
								droppedItem.setCanMobPickup(false);
							}
						}
					})))
			.withSubcommand(new CommandAPICommand("description")
				.withSubcommand(new CommandAPICommand("get")
					.withPermission(CommandPermission.fromString("los.bestiary.description"))
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.executesPlayer((sender, args) -> {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						} else {
							ItemStack item = new ItemStack(Material.BLAZE_POWDER);
							item.lore(soul.getDescription());
							Location location = sender.getLocation();
							if (!sender.getInventory().addItem(item).isEmpty()) {
								Item droppedItem = location.getWorld().dropItem(location, item);
								droppedItem.setPickupDelay(0);
								droppedItem.setCanMobPickup(false);
							}
						}
					})))
			.register();
	}

	public static void registerWriteAccessCommands() {
		final String command = "bestiary";

		TextArgument loreArg = new TextArgument("lore");
		TextArgument descriptionArg = new TextArgument("description");
		ObjectiveArgument objectiveArg = new ObjectiveArgument("objective");
		IntegerArgument minScoreArg = new IntegerArgument("min_score");

		new CommandAPICommand(command)
			.withSubcommand(new CommandAPICommand("lore")
				.withPermission(CommandPermission.fromString("los.bestiary.lore"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(loreArg)
				.executesPlayer((sender, args) -> {
					String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
					SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
					if (soul == null) {
						throw CommandAPI.failWithString("Mob '" + name + "' not found");
					} else {
						Component component = Component.text(args.getByArgument(loreArg));
						soul.setLore(List.of(component), sender);
					}
				})
				.executesProxy((sender, args) -> {
					if (sender.getCallee() instanceof Player player) {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						} else {
							Component component = Component.text(args.getByArgument(loreArg));
							soul.setLore(List.of(component), player);
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
							String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
							SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
							if (soul == null) {
								throw CommandAPI.failWithString("Mob '" + name + "' not found");
							}
							soul.setLore(new ArrayList<>(), sender);
						})))
				.withSubcommand(new CommandAPICommand("lore")
					.withSubcommand(new CommandAPICommand("frommainhand")
						.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
						.withPermission(CommandPermission.fromString("los.bestiary.lore"))
						.executesPlayer((sender, args) -> {
							ItemStack item = sender.getInventory().getItemInMainHand();
							if (!item.getItemMeta().hasLore()) {
								throw CommandAPI.failWithString("You need a valid item with lore text!");
							}
							List<Component> lore = item.lore();
							String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
							SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
							if (soul == null) {
								throw CommandAPI.failWithString("Mob '" + name + "' not found");
							}
							soul.setLore(lore, sender);
						})))
			.withSubcommand(new CommandAPICommand("lore")
				.withSubcommand(new CommandAPICommand("prereq")
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.withArguments(objectiveArg)
					.withArguments(minScoreArg)
					.withPermission(CommandPermission.fromString("los.bestiary.lore"))
					.executesPlayer((sender, args) -> {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						}
						soul.setLorePrereq(args.getByArgument(objectiveArg).getName(), args.getByArgument(minScoreArg), sender);
					}))
			)
			.withSubcommand(new CommandAPICommand("description")
				.withPermission(CommandPermission.fromString("los.bestiary.description"))
				.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
				.withArguments(descriptionArg)
				.executesPlayer((sender, args) -> {
					String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
					SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
					if (soul == null) {
						throw CommandAPI.failWithString("Mob '" + name + "' not found");
					} else {
						Component component = Component.text(args.getByArgument(descriptionArg));
						soul.setDescription(List.of(component), sender);
					}
				})
				.executesProxy((sender, args) -> {
					if (sender.getCallee() instanceof Player player) {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						} else {
							Component component = Component.text(args.getByArgument(descriptionArg));
							soul.setDescription(List.of(component), player);
						}
					} else {
						throw CommandAPI.failWithString("Callee must be instance of Player");
					}
				}))
			.withSubcommand(new CommandAPICommand("description")
				.withSubcommand(new CommandAPICommand("clear")
					.withPermission(CommandPermission.fromString("los.bestiary.description"))
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.executesPlayer((sender, args) -> {
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						}
						soul.setDescription(new ArrayList<>(), sender);
					})))
			.withSubcommand(new CommandAPICommand("description")
				.withSubcommand(new CommandAPICommand("frommainhand")
					.withArguments(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION))
					.withPermission(CommandPermission.fromString("los.bestiary.description"))
					.executesPlayer((sender, args) -> {
						ItemStack item = sender.getInventory().getItemInMainHand();
						if (!item.getItemMeta().hasLore()) {
							throw CommandAPI.failWithString("You need a valid item with lore text!");
						}
						List<Component> lore = item.lore();
						String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LibraryOfSoulsCommand.LIST_MOBS_FUNCTION));
						SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
						if (soul == null) {
							throw CommandAPI.failWithString("Mob '" + name + "' not found");
						}
						soul.setDescription(lore, sender);
					})))
			.register();
	}

	private static class LoreTestInventory {
		private final Inventory mInv;

		private LoreTestInventory(SoulEntry soul, Player player) {
			mInv = Bukkit.createInventory(player, 54);
			ItemStack loreItem = getLoreItem(soul, player);
			for (int i = 0; i < 54; i++) {
				mInv.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
			}

			mInv.setItem(33, loreItem);
		}

		private void openInventory(Player player) {
			player.openInventory(mInv);
		}

		private ItemStack getLoreItem(SoulEntry soul, Player player) {
			List<Component> lore = soul.getLore();

			ItemStack loreItem = new ItemStack(Material.BOOK);
			ItemMeta meta = loreItem.getItemMeta();
			meta.displayName(Component.text("Lore", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));

			if (lore == null || lore.isEmpty()) {
				List<Component> itemLore = new ArrayList<>();
				itemLore.add(Component.text("No lore provided.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, true));

				meta.lore(itemLore);
				loreItem.setItemMeta(meta);
				return loreItem;
			} else if (!soul.canSeeLore(player)) {
				List<Component> itemLore = new ArrayList<>();
				itemLore.add(Component.text("Required scores not present to view lore.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, true));
				if (soul.getLorePrereqObjective() != null) {
					itemLore.add(Component.text("Need " + soul.getLorePrereqObjective() + " score of at least " + soul.getLorePrereqMinScore() + " to view.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, true));
				}

				meta.lore(itemLore);
				loreItem.setItemMeta(meta);
				return loreItem;
			}

			meta.lore(lore);
			loreItem.setItemMeta(meta);

			return loreItem;
		}
	}
}
