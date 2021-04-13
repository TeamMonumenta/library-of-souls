package com.playmonumenta.libraryofsouls.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.SoulsInventory;
import com.playmonumenta.libraryofsouls.SpawnerInventory;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class LibraryOfSoulsCommand {
	/* Several sub commands have this same tab completion */
	public static final Function<CommandSender, String[]> LIST_MOBS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobNames().toArray(new String[SoulsDatabase.getInstance().listMobNames().size()]);
	private static final String COMMAND = "los";

	public static void register() {
		List<Argument> arguments = new ArrayList<>();

		/* los open */
		arguments.add(new MultiLiteralArgument("open"));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.open"))
			.withArguments(arguments)
			.executes((sender, args) -> {
                Player player = getPlayer(sender);
                (new SoulsInventory(player, SoulsDatabase.getInstance().getSouls(), ""))
                    .openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los get <name> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("get"));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.get"))
			.withArguments(arguments)
			.executes((sender, args) -> {
                PlayerInventory inv = getPlayer(sender).getInventory();
                if (inv.firstEmpty() == -1) {
                    CommandAPI.fail("Your inventory is full!");
                }
                inv.addItem(getSoul((String)args[1]).getBoS());
			})
			.register();

		/* los history <name> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("history"));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.history"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				(new SoulsInventory(player, getSoul((String)args[1]).getHistory(), "History"))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los summon <location> <name> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("summon"));
		arguments.add(new LocationArgument("location"));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				getSoul((String)args[2]).summon((Location)args[1]);
			})
			.register();

		/* los search <area> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("search"));
		arguments.add(new StringArgument("area").overrideSuggestions((sender) -> SoulsDatabase.getInstance().listMobLocations().toArray(String[]::new)));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String area = (String)args[1];
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(area);
				if (souls == null) {
					CommandAPI.fail("Area '" + area + "' not found");
				}
				(new SoulsInventory(player, souls, area))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los searchtype <id> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("searchtype"));
		arguments.add(new StringArgument("id").overrideSuggestions((sender) -> SoulsDatabase.getInstance().listMobTypes().toArray(new String[0])));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String id = (String)args[1];
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByType(id);
				if (souls == null) {
					CommandAPI.fail("Mob type '" + id + "' not found");
				}
				(new SoulsInventory(player, souls, id))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los spawner <name> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("spawner"));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.spawner"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				Soul soul = SoulsDatabase.getInstance().getSoul((String)args[1]);
				SpawnerInventory.openSpawnerInventory(soul, player, null);
			})
			.register();
	}

	public static void registerWriteAccessCommands() {
		List<Argument> arguments = new ArrayList<>();

		/* los add */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("add"));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.add"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);
				if (bos == null) {
					CommandAPI.fail("You must be holding a Book of Souls");
				}

				SoulsDatabase.getInstance().add(player, bos);
			})
			.register();

		/* los update */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("update"));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.update"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);
				if (bos == null) {
					CommandAPI.fail("You must be holding a Book of Souls");
				}

				SoulsDatabase.getInstance().update(player, bos);
			})
			.register();

		/* los del <name> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("del"));
		arguments.add(new StringArgument("mobLabel").overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.del"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				SoulsDatabase.getInstance().del(sender, (String)args[1]);
			})
			.register();
	}

	public static SoulEntry getSoul(String name) throws WrapperCommandSyntaxException {
		SoulEntry soul = SoulsDatabase.getInstance().getSoul(name);
		if (soul != null) {
			return soul;
		}

		CommandAPI.fail("Soul '" + name + "' not found");
		return null;
	}

	private static Player getPlayer(CommandSender sender) throws WrapperCommandSyntaxException {
		if (sender instanceof Player) {
			return (Player) sender;
		} else if ((sender instanceof ProxiedCommandSender) && (((ProxiedCommandSender)sender).getCallee() instanceof Player)) {
			return (Player) ((ProxiedCommandSender)sender).getCallee();
		}

		CommandAPI.fail("This command must be run by / as a player");
		return null;
	}

	private static BookOfSouls getBos(Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (BookOfSouls.isValidBook(item)) {
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos != null) {
				return bos;
			}
			CommandAPI.fail("That Book of Souls is corrupted!");
		}
		CommandAPI.fail("You must be holding a Book of Souls!");
		return null;
	}
}
