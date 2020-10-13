package com.playmonumenta.libraryofsouls.commands;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.function.Function;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
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
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class LibraryOfSoulsCommand implements Listener {
	/* Several sub commands have this same tab completion */
	public static final Function<CommandSender, String[]> LIST_MOBS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobNames().toArray(new String[SoulsDatabase.getInstance().listMobNames().size()]);
	private static final String COMMAND = "los";

	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		/* los open */
		arguments.put("open", new LiteralArgument("open"));
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
		arguments.put("get", new LiteralArgument("get"));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.get"))
			.withArguments(arguments)
			.executes((sender, args) -> {
                PlayerInventory inv = getPlayer(sender).getInventory();
                if (inv.firstEmpty() == -1) {
                    CommandAPI.fail("Your inventory is full!");
                }
                inv.addItem(getSoul((String)args[0]).getBoS());
			})
			.register();

		/* los history <name> */
		arguments.clear();
		arguments.put("history", new LiteralArgument("history"));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.history"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				(new SoulsInventory(player, getSoul((String)args[0]).getHistory(), "History"))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los summon <location> <name> */
		arguments.clear();
		arguments.put("summon", new LiteralArgument("summon"));
		arguments.put("location", new LocationArgument());
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				getSoul((String)args[1]).summon((Location)args[0]);
			})
			.register();

		/* los search <area> */
		arguments.clear();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("area", new DynamicSuggestedStringArgument(() -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[0])));
		api.register("los", CommandPermission.fromString("los.search"), arguments, (sender, args) -> {
			Player player = getPlayer(sender);
			String area = (String)args[0];
			List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
			if (souls == null) {
				CommandAPI.fail("Area '" + area + "' not found");
			}
			(new SoulsInventory(player, souls, area))
				.openInventory(player, LibraryOfSouls.getInstance());
		});
		
		arguments.put("area", new StringArgument().overrideSuggestions((sender) -> SoulsDatabase.getInstance().listMobLocations().toArray(new String[0])));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String area = (String)args[0];
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation((String)args[0]);
				if (souls == null) {
					CommandAPI.fail("Area '" + area + "' not found");
				}
				(new SoulsInventory(player, souls, area))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los spawner <name> */
		arguments.clear();
		arguments.put("spawner", new LiteralArgument("spawner"));
		arguments.put("name", new DynamicSuggestedStringArgument(LIST_MOBS_FUNCTION));
		api.register("los", CommandPermission.fromString("los.spawner"), arguments, (sender, args) -> {
			Player player = getPlayer(sender);
			Soul soul = SoulsDatabase.getInstance().getSoul((String)args[0]);
			SpawnerInventory.openSpawnerInventory(soul, player, null);
		});
		
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.spawner"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				Soul soul = SoulsDatabase.getInstance().getSoul((String)args[0]);
				SpawnerInventory.openSpawnerInventory(soul, player, null);
			})
			.register();
	}

	public static void registerWriteAccessCommands() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		/* los add */
		arguments.clear();
		arguments.put("add", new LiteralArgument("add"));
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
		arguments.put("update", new LiteralArgument("update"));
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
		arguments.put("del", new LiteralArgument("del"));
		arguments.put("mobLabel", new StringArgument().overrideSuggestions(LIST_MOBS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.del"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				SoulsDatabase.getInstance().del(sender, (String)args[0]);
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
