package com.playmonumenta.libraryofsouls.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSoulsAPI;
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
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.arguments.ScoreHolderArgument.ScoreHolderType;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class LibraryOfSoulsCommand {
	/* Several sub commands have this same tab completion */
	public static final Function<CommandSender, String[]> LIST_MOBS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listMobNames().stream().toArray(String[]::new);
	public static final Function<CommandSender, String[]> LIST_SOUL_PARTIES_FUNCTION = (sender) -> SoulsDatabase.getInstance().listSoulPartyNames().stream().toArray(String[]::new);
	public static final Function<CommandSender, String[]> LIST_SOUL_POOLS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listSoulPoolNames().stream().toArray(String[]::new);
	public static final Function<CommandSender, String[]> LIST_SOUL_GROUPS_FUNCTION = (sender) -> SoulsDatabase.getInstance().listSoulGroupNames().stream().toArray(String[]::new);
	private static final String COMMAND = "los";
	private static final Pattern VALID_SOUL_GROUP_LABEL = Pattern.compile("[0-9A-Za-z_]+");

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

		/* los party <partyLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("party"));
		arguments.add(new ScoreHolderArgument("partyLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_PARTIES_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.party"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				String partyLabel = (String)args[1];
				SoulsDatabase database = SoulsDatabase.getInstance();
				sender.sendMessage(Component.text("Party counts:"));
				for (Map.Entry<String, Integer> entry : database.getSoulParty(partyLabel).getEntryCounts().entrySet()) {
					String entryLabel = entry.getKey();
					String entryCount = Integer.toString(entry.getValue());
					String entryCommand = "/los updateparty " + partyLabel + " " + entryLabel + " " + entryCount;
					sender.sendMessage(Component.text("- " + entryCount + "x " + entryLabel)
						.clickEvent(ClickEvent.suggestCommand(entryCommand))
						.hoverEvent(Component.text(entryCommand)));
				}
			})
			.register();

		/* los pool <poolLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("pool"));
		arguments.add(new ScoreHolderArgument("poolLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_POOLS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.pool"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				String poolLabel = (String)args[1];
				SoulsDatabase database = SoulsDatabase.getInstance();
				sender.sendMessage(Component.text("Pool weights:"));
				long totalWeight = 0;
				for (Map.Entry<String, Integer> entry : database.getSoulPool(poolLabel).getEntryWeights().entrySet()) {
					String entryLabel = entry.getKey();
					int weight = entry.getValue();
					totalWeight += weight;
					String entryWeight = Integer.toString(weight);
					String entryCommand = "/los updatepool " + poolLabel + " " + entryLabel + " " + entryWeight;
					sender.sendMessage(Component.text("- " + entryWeight + "x " + entryLabel)
						.clickEvent(ClickEvent.suggestCommand(entryCommand))
						.hoverEvent(Component.text(entryCommand)));
				}
				sender.sendMessage(Component.text("Total weight: " + Long.toString(totalWeight)));
			})
			.register();

		/* los averagegroup <groupLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("averagegroup"));
		arguments.add(new ScoreHolderArgument("group", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_GROUPS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.averagegroup"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				String groupLabel = (String)args[1];
				SoulsDatabase database = SoulsDatabase.getInstance();
				sender.sendMessage(Component.text("Pool weights:"));
				for (Map.Entry<Soul, Double> entry : database.getSoulGroup(groupLabel).getAverageEntries().entrySet()) {
					Component name = entry.getKey().getName();
					double aveCount = entry.getValue();
					sender.sendMessage(Component.text("- " + String.format("%04.2f", aveCount) + "x ").append(name));
				}
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

		/* los search */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("search"));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(null);
				(new SoulsInventory(player, souls, "No Location"))
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los search <area> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("search"));
		arguments.add(new StringArgument("area").overrideSuggestions((sender) -> SoulsDatabase.getInstance().listMobLocations().stream().toArray(String[]::new)));
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
		arguments.add(new StringArgument("id").overrideSuggestions((sender) -> SoulsDatabase.getInstance().listMobTypes().stream().toArray(String[]::new)));
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

		/* los addparty <partyLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("addparty"));
		arguments.add(new ScoreHolderArgument("partyLabel", ScoreHolderType.SINGLE).overrideSuggestions(new String[0]));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addparty"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = (String)args[1];
				String partyLabelNoPrefix = partyLabel;
				if (partyLabelNoPrefix.startsWith(LibraryOfSoulsAPI.SOUL_PARTY_PREFIX)) {
					partyLabelNoPrefix = partyLabelNoPrefix.substring(1);
				}
				if (!VALID_SOUL_GROUP_LABEL.matcher(partyLabelNoPrefix).matches()) {
					CommandAPI.fail("Soul party label must contain only [A-Za-z0-9_], prefixed with " + LibraryOfSoulsAPI.SOUL_PARTY_PREFIX);
				}

				SoulsDatabase.getInstance().addParty(player, partyLabel);
			})
			.register();

		/* los updateparty <partyLabel> <entryLabel> <count> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("updateparty"));
		arguments.add(new ScoreHolderArgument("partyLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_PARTIES_FUNCTION));
		arguments.add(new ScoreHolderArgument("entryLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_GROUPS_FUNCTION));
		arguments.add(new IntegerArgument("count", 0));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.updateparty"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = (String)args[1];
				String entryLabel = (String)args[2];
				int count = (int)args[3];

				SoulsDatabase.getInstance().updateParty(player, partyLabel, entryLabel, count);
			})
			.register();

		/* los delparty <partyLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("delparty"));
		arguments.add(new ScoreHolderArgument("partyLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_PARTIES_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delparty"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				String partyLabel = (String)args[1];
				SoulsDatabase.getInstance().delParty(sender, partyLabel);
			})
			.register();

		/* los addpool <poolLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("addpool"));
		arguments.add(new ScoreHolderArgument("poolLabel", ScoreHolderType.SINGLE).overrideSuggestions(new String[0]));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addpool"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = (String)args[1];
				String poolLabelNoPrefix = poolLabel;
				if (poolLabelNoPrefix.startsWith(LibraryOfSoulsAPI.SOUL_POOL_PREFIX)) {
					poolLabelNoPrefix = poolLabelNoPrefix.substring(1);
				}
				if (!VALID_SOUL_GROUP_LABEL.matcher(poolLabelNoPrefix).matches()) {
					CommandAPI.fail("Soul pool label must contain only [A-Za-z0-9_], prefixed with " + LibraryOfSoulsAPI.SOUL_POOL_PREFIX);
				}

				SoulsDatabase.getInstance().addPool(player, poolLabel);
			})
			.register();

		/* los updatepool <poolLabel> <entryLabel> <weight> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("updatepool"));
		arguments.add(new ScoreHolderArgument("poolLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_POOLS_FUNCTION));
		arguments.add(new ScoreHolderArgument("entryLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_GROUPS_FUNCTION));
		arguments.add(new IntegerArgument("weight", 0));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.updatepool"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = (String)args[1];
				String entryLabel = (String)args[2];
				int count = (int)args[3];

				SoulsDatabase.getInstance().updatePool(player, poolLabel, entryLabel, count);
			})
			.register();

		/* los delpool <poolLabel> */
		arguments.clear();
		arguments.add(new MultiLiteralArgument("delpool"));
		arguments.add(new ScoreHolderArgument("poolLabel", ScoreHolderType.SINGLE).overrideSuggestions(LIST_SOUL_POOLS_FUNCTION));
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delpool"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				String poolLabel = (String)args[1];
				SoulsDatabase.getInstance().delPool(sender, poolLabel);
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

	public static Player getPlayer(CommandSender sender) throws WrapperCommandSyntaxException {
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
