package com.playmonumenta.libraryofsouls.commands;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.LibraryOfSoulsAPI;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulGroup;
import com.playmonumenta.libraryofsouls.SoulPartyEntry;
import com.playmonumenta.libraryofsouls.SoulPoolEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;
import com.playmonumenta.libraryofsouls.SoulsInventory;
import com.playmonumenta.libraryofsouls.SpawnerInventory;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BoundingBox;

public class LibraryOfSoulsCommand {
	/* Several sub commands have this same tab completion */
	public static final ArgumentSuggestions LIST_MOBS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobNames().stream().toArray(String[]::new));
	public static final ArgumentSuggestions LIST_SOUL_PARTIES_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPartyNames().stream().toArray(String[]::new));
	public static final ArgumentSuggestions LIST_SOUL_POOLS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPoolNames().stream().toArray(String[]::new));
	public static final ArgumentSuggestions LIST_SOUL_GROUPS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulGroupNames().stream().toArray(String[]::new));
	private static final String COMMAND = "los";
	private static final Pattern VALID_SOUL_GROUP_LABEL = Pattern.compile("[0-9A-Za-z_]+");

	public static void register() {
		/* los open */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.open"))
			.withArguments(new MultiLiteralArgument("open"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				new SoulsInventory(player, SoulsDatabase.getInstance().getSouls(), "")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los get <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.get"))
			.withArguments(new MultiLiteralArgument("get"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				PlayerInventory inv = getPlayer(sender).getInventory();
				if (inv.firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				inv.addItem(getSoul((String)args[1]).getBoS());
			})
			.register();

		/* los party <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.party"))
			.withArguments(new MultiLiteralArgument("party"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.executes((sender, args) -> {
				String partyLabel = (String)args[1];
				SoulsDatabase database = SoulsDatabase.getInstance();
				sender.sendMessage(Component.text("Party counts:"));
				SoulPartyEntry party = database.getSoulParty(partyLabel);
				if (party == null) {
					throw CommandAPI.failWithString("Party '" + partyLabel + "' does not exist");
				}
				for (Map.Entry<String, Integer> entry : party.getEntryCounts().entrySet()) {
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
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.pool"))
			.withArguments(new MultiLiteralArgument("pool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
			.executes((sender, args) -> {
				String poolLabel = (String)args[1];
				SoulsDatabase database = SoulsDatabase.getInstance();
				sender.sendMessage(Component.text("Pool weights:"));
				long totalWeight = 0;
				SoulPoolEntry pool = database.getSoulPool(poolLabel);
				if (pool == null) {
					throw CommandAPI.failWithString("Pool '" + poolLabel + "' does not exist");
				}
				for (Map.Entry<String, Integer> entry : pool.getEntryWeights().entrySet()) {
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
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.averagegroup"))
			.withArguments(new MultiLiteralArgument("averagegroup"))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.executes((sender, args) -> {
				String groupLabel = (String)args[1];
				sender.sendMessage(Component.text("Pool weights:"));
				for (Map.Entry<Soul, Double> entry : getSoulGroup(groupLabel).getAverageSouls().entrySet()) {
					Component name = entry.getKey().getName();
					double aveCount = entry.getValue();
					sender.sendMessage(Component.text("- " + String.format("%04.2f", aveCount) + "x ").append(name));
				}
			})
			.register();

		/* los history <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.history"))
			.withArguments(new MultiLiteralArgument("history"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				new SoulsInventory(player, getSoul((String)args[1]).getHistory(), "History")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los summon <location> <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(new MultiLiteralArgument("summon"))
			.withArguments(new LocationArgument("location"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				getSoul((String)args[2]).summon((Location)args[1]);
			})
			.register();

		/* los summongroup <name> <pos1> <pos2> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summongroup"))
			.withArguments(new MultiLiteralArgument("summongroup"))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(new LocationArgument("pos1"))
			.withArguments(new LocationArgument("pos2"))
			.executes((sender, args) -> {
				Location pos1 = (Location)args[2];
				Location pos2 = (Location)args[3];
				BoundingBox bb = BoundingBox.of(pos1, pos2);
				getSoulGroup((String)args[1]).summonGroup(new Random(), pos1.getWorld(), bb);
			})
			.register();

		/* los search */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(new MultiLiteralArgument("search"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(null);
				if (souls == null) {
					throw CommandAPI.failWithString("Empty area not found - this is a code bug, should not be possible to get here");
				}
				new SoulsInventory(player, souls, "No Location")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los search <area> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(new MultiLiteralArgument("search"))
			.withArguments(new StringArgument("area").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobLocations().stream().toArray(String[]::new))))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String area = (String)args[1];
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(area);
				if (souls == null) {
					throw CommandAPI.failWithString("Area '" + area + "' not found");
				}
				new SoulsInventory(player, souls, area)
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los searchtype <id> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(new MultiLiteralArgument("searchtype"))
			.withArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobTypes().stream().toArray(String[]::new))))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String id = (String)args[1];
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByType(id);
				if (souls == null) {
					throw CommandAPI.failWithString("Mob type '" + id + "' not found");
				}
				new SoulsInventory(player, souls, id)
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los spawner <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.spawner"))
			.withArguments(new MultiLiteralArgument("spawner"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String name = (String)args[1];
				Soul soul = SoulsDatabase.getInstance().getSoul(name);
				if (soul == null) {
					throw CommandAPI.failWithString("Soul '" + name + "' not found");
				}
				SpawnerInventory.openSpawnerInventory(soul, player, null);
			})
			.register();
	}

	public static void registerWriteAccessCommands() {
		/* los autoupdate <location> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.autoupdate"))
			.withArguments(new MultiLiteralArgument("autoupdate"))
			.executes((sender, args) -> {
				if (!(sender instanceof Player)) {
					throw CommandAPI.failWithString("autoupdate must be run by a player");
				}
				SoulsDatabase.getInstance().autoUpdate(sender, ((Player) sender).getLocation());
			})
			.register();

		/* los add */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.add"))
			.withArguments(new MultiLiteralArgument("add"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);
				if (bos == null) {
					throw CommandAPI.failWithString("You must be holding a Book of Souls");
				}

				SoulsDatabase.getInstance().add(player, bos);
			})
			.register();

		/* los update */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.update"))
			.withArguments(new MultiLiteralArgument("update"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);
				if (bos == null) {
					throw CommandAPI.failWithString("You must be holding a Book of Souls");
				}

				SoulsDatabase.getInstance().update(player, bos);
			})
			.register();

		/* los del <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.del"))
			.withArguments(new MultiLiteralArgument("del"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				SoulsDatabase.getInstance().del(sender, (String)args[1]);
			})
			.register();

		/* los addparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addparty"))
			.withArguments(new MultiLiteralArgument("addparty"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = (String)args[1];
				String partyLabelNoPrefix = partyLabel;
				if (partyLabelNoPrefix.startsWith(LibraryOfSoulsAPI.SOUL_PARTY_PREFIX)) {
					partyLabelNoPrefix = partyLabelNoPrefix.substring(1);
				}
				if (!VALID_SOUL_GROUP_LABEL.matcher(partyLabelNoPrefix).matches()) {
					throw CommandAPI.failWithString("Soul party label must contain only [A-Za-z0-9_], prefixed with " + LibraryOfSoulsAPI.SOUL_PARTY_PREFIX);
				}

				SoulsDatabase.getInstance().addParty(player, partyLabel);
			})
			.register();

		/* los updateparty <partyLabel> <entryLabel> <count> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.updateparty"))
			.withArguments(new MultiLiteralArgument("updateparty"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(new IntegerArgument("count", 0))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = (String)args[1];
				String entryLabel = (String)args[2];
				int count = (int)args[3];

				SoulsDatabase.getInstance().updateParty(player, partyLabel, entryLabel, count);
			})
			.register();

		/* los delparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delparty"))
			.withArguments(new MultiLiteralArgument("delparty"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.executes((sender, args) -> {
				String partyLabel = (String)args[1];
				SoulsDatabase.getInstance().delParty(sender, partyLabel);
			})
			.register();

		/* los addpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addpool"))
			.withArguments(new MultiLiteralArgument("addpool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = (String)args[1];
				String poolLabelNoPrefix = poolLabel;
				if (poolLabelNoPrefix.startsWith(LibraryOfSoulsAPI.SOUL_POOL_PREFIX)) {
					poolLabelNoPrefix = poolLabelNoPrefix.substring(1);
				}
				if (!VALID_SOUL_GROUP_LABEL.matcher(poolLabelNoPrefix).matches()) {
					throw CommandAPI.failWithString("Soul pool label must contain only [A-Za-z0-9_], prefixed with " + LibraryOfSoulsAPI.SOUL_POOL_PREFIX);
				}

				SoulsDatabase.getInstance().addPool(player, poolLabel);
			})
			.register();

		/* los updatepool <poolLabel> <entryLabel> <weight> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.updatepool"))
			.withArguments(new MultiLiteralArgument("updatepool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(new IntegerArgument("weight", 0))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = (String)args[1];
				String entryLabel = (String)args[2];
				int count = (int)args[3];

				SoulsDatabase.getInstance().updatePool(player, poolLabel, entryLabel, count);
			})
			.register();

		/* los delpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delpool"))
			.withArguments(new MultiLiteralArgument("delpool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
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

		throw CommandAPI.failWithString("Soul '" + name + "' not found");
	}

	public static SoulGroup getSoulGroup(String name) throws WrapperCommandSyntaxException {
		SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(name);
		if (group != null) {
			return group;
		}

		throw CommandAPI.failWithString("Soul group '" + name + "' not found");
	}

	public static Player getPlayer(CommandSender sender) throws WrapperCommandSyntaxException {
		if (sender instanceof Player) {
			return (Player) sender;
		} else if ((sender instanceof ProxiedCommandSender) && (((ProxiedCommandSender)sender).getCallee() instanceof Player)) {
			return (Player) ((ProxiedCommandSender)sender).getCallee();
		}

		throw CommandAPI.failWithString("This command must be run by / as a player");
	}

	private static BookOfSouls getBos(Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (BookOfSouls.isValidBook(item)) {
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos != null) {
				return bos;
			}
			throw CommandAPI.failWithString("That Book of Souls is corrupted!");
		}
		throw CommandAPI.failWithString("You must be holding a Book of Souls!");
	}
}
