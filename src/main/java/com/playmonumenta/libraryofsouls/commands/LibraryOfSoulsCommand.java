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
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
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
	public static final ArgumentSuggestions<CommandSender> LIST_MOBS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_PARTIES_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPartyNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_POOLS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPoolNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_GROUPS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulGroupNames().toArray(String[]::new));
	private static final String COMMAND = "los";
	private static final Pattern VALID_SOUL_GROUP_LABEL = Pattern.compile("[0-9A-Za-z_]+");

	public static final Argument<String> mobLabelArg = new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION);
	public static final Argument<String> partyLabelArg = new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION);
	public static final Argument<String> poolLabelArg = new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION);
	public static final Argument<String> groupLabelArg = new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION);
	// No clue why these are scoreholder arguments, but not going to take the risk of changing them

	public static void register() {
		LocationArgument locationArg = new LocationArgument("location");
		LocationArgument pos1Arg = new LocationArgument("pos1");
		LocationArgument pos2Arg = new LocationArgument("pos2");
		Argument<String> areaArg = new StringArgument("area").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobLocations().toArray(String[]::new)));
		Argument<String> idArg = new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobTypes().toArray(String[]::new)));
		Argument<Integer> indexArg = new IntegerArgument("index", 1);

		/* los open */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.open"))
			.withArguments(new LiteralArgument("open"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				new SoulsInventory(player, SoulsDatabase.getInstance().getSouls(), "")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los get <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.get"))
			.withArguments(new LiteralArgument("get"))
			.withArguments(mobLabelArg)
			.executes((sender, args) -> {
				PlayerInventory inv = getPlayer(sender).getInventory();
				if (inv.firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				inv.addItem(getSoul(args.getByArgument(mobLabelArg)).getBoS());
			})
			.register();

		/* los get <index> - Get soul by numeric index */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.get"))
			.withArguments(new LiteralArgument("get"))
			.withArguments(indexArg)
			.executes((sender, args) -> {
				PlayerInventory inv = getPlayer(sender).getInventory();
				if (inv.firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				int index = args.getByArgument(indexArg);
				inv.addItem(getSoulByIndex(index).getBoS());
			})
			.register();

		/* los party <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.party"))
			.withArguments(new LiteralArgument("party"))
			.withArguments(partyLabelArg)
			.executes((sender, args) -> {
				String partyLabel = args.getByArgument(partyLabelArg);
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
			.withArguments(new LiteralArgument("pool"))
			.withArguments(poolLabelArg)
			.executes((sender, args) -> {
				String poolLabel = args.getByArgument(poolLabelArg);
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
				sender.sendMessage(Component.text("Total weight: " + totalWeight));
			})
			.register();

		/* los averagegroup <groupLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.averagegroup"))
			.withArguments(new LiteralArgument("averagegroup"))
			.withArguments(groupLabelArg)
			.executes((sender, args) -> {
				String groupLabel = args.getByArgument(groupLabelArg);
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
			.withArguments(new LiteralArgument("history"))
			.withArguments(mobLabelArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				new SoulsInventory(player, getSoul(args.getByArgument(mobLabelArg)).getHistory(), "History")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los summon <location> <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(new LiteralArgument("summon"))
			.withArguments(locationArg)
			.withArguments(mobLabelArg)
			.executes((sender, args) -> {
				getSoul(args.getByArgument(mobLabelArg)).summon(args.getByArgument(locationArg));
			})
			.register();

		/* los summon <location> <index> - Summon soul by numeric index */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(new LiteralArgument("summon"))
			.withArguments(locationArg)
			.withArguments(indexArg)
			.executes((sender, args) -> {
				int index = args.getByArgument(indexArg);
				getSoulByIndex(index).summon(args.getByArgument(locationArg));
			})
			.register();

		/* los summongroup <name> <pos1> <pos2> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summongroup"))
			.withArguments(new LiteralArgument("summongroup"))
			.withArguments(groupLabelArg)
			.withArguments(pos1Arg)
			.withArguments(pos2Arg)
			.executes((sender, args) -> {
				Location pos1 = args.getByArgument(pos1Arg);
				Location pos2 = args.getByArgument(pos2Arg);
				BoundingBox bb = BoundingBox.of(pos1, pos2);
				getSoulGroup(args.getByArgument(groupLabelArg)).summonGroup(new Random(), pos1.getWorld(), bb);
			})
			.register();

		/* los search */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(new LiteralArgument("search"))
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
			.withArguments(new LiteralArgument("search"))
			.withArguments(areaArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String area = args.getByArgument(areaArg);
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
			.withArguments(new LiteralArgument("searchtype"))
			.withArguments(idArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String id = args.getByArgument(idArg);
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
			.withArguments(new LiteralArgument("spawner"))
			.withArguments(mobLabelArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String name = args.getByArgument(mobLabelArg);
				Soul soul = SoulsDatabase.getInstance().getSoul(name);
				if (soul == null) {
					throw CommandAPI.failWithString("Soul '" + name + "' not found");
				}
				SpawnerInventory.openSpawnerInventory(soul, player, null);
			})
			.register();
	}

	public static void registerWriteAccessCommands() {
		IntegerArgument countArg = new IntegerArgument("count", 0);
		IntegerArgument weightArg = new IntegerArgument("weight", 0);

		/* los autoupdate <location> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.autoupdate"))
			.withArguments(new LiteralArgument("autoupdate"))
			.executesPlayer((sender, args) -> {
				SoulsDatabase.getInstance().autoUpdate(sender, sender.getLocation());
			})
			.register();

		/* los add */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.add"))
			.withArguments(new LiteralArgument("add"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);

				SoulsDatabase.getInstance().add(player, bos);
			})
			.register();

		/* los update */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.update"))
			.withArguments(new LiteralArgument("update"))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				BookOfSouls bos = getBos(player);

				SoulsDatabase.getInstance().update(player, bos);
			})
			.register();

		/* los del <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.del"))
			.withArguments(new LiteralArgument("del"))
			.withArguments(mobLabelArg)
			.executes((sender, args) -> {
				SoulsDatabase.getInstance().del(sender, args.getByArgument(mobLabelArg));
			})
			.register();

		/* los addparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addparty"))
			.withArguments(new LiteralArgument("addparty"))
			.withArguments(partyLabelArg.replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = args.getByArgument(partyLabelArg);
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
			.withArguments(new LiteralArgument("updateparty"))
			.withArguments(partyLabelArg)
			.withArguments(groupLabelArg)
			.withArguments(countArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = args.getByArgument(partyLabelArg);
				String entryLabel = args.getByArgument(groupLabelArg);
				int count = args.getByArgument(countArg);

				SoulsDatabase.getInstance().updateParty(player, partyLabel, entryLabel, count);
			})
			.register();

		/* los delparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delparty"))
			.withArguments(new LiteralArgument("delparty"))
			.withArguments(partyLabelArg)
			.executes((sender, args) -> {
				String partyLabel = args.getByArgument(partyLabelArg);
				SoulsDatabase.getInstance().delParty(sender, partyLabel);
			})
			.register();

		/* los addpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addpool"))
			.withArguments(new LiteralArgument("addpool"))
			.withArguments(poolLabelArg.replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = args.getByArgument(poolLabelArg);
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
			.withArguments(new LiteralArgument("updatepool"))
			.withArguments(poolLabelArg)
			.withArguments(groupLabelArg)
			.withArguments(weightArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = args.getByArgument(poolLabelArg);
				String entryLabel = args.getByArgument(groupLabelArg);
				int count = args.getByArgument(weightArg);

				SoulsDatabase.getInstance().updatePool(player, poolLabel, entryLabel, count);
			})
			.register();

		/* los delpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delpool"))
			.withArguments(new LiteralArgument("delpool"))
			.withArguments(poolLabelArg)
			.executes((sender, args) -> {
				String poolLabel = args.getByArgument(poolLabelArg);
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

	/**
	 * Helper method to get a soul by its numeric index with proper error handling
	 * @param index The numeric index of the soul to retrieve
	 * @return The SoulEntry with that index
	 * @throws WrapperCommandSyntaxException if no soul is found with that index
	 */
	public static SoulEntry getSoulByIndex(int index) throws WrapperCommandSyntaxException {
		SoulEntry soul = SoulsDatabase.getInstance().getSoulByIndex(index);
		if (soul != null) {
			return soul;
		}

		throw CommandAPI.failWithString("No soul found with index " + index);
	}

	public static SoulGroup getSoulGroup(String name) throws WrapperCommandSyntaxException {
		SoulGroup group = SoulsDatabase.getInstance().getSoulGroup(name);
		if (group != null) {
			return group;
		}

		throw CommandAPI.failWithString("Soul group '" + name + "' not found");
	}

	public static Player getPlayer(CommandSender sender) throws WrapperCommandSyntaxException {
		if (sender instanceof Player player) {
			return player;
		} else if (sender instanceof ProxiedCommandSender proxiedCommandSender && proxiedCommandSender.getCallee() instanceof Player player) {
			return player;
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
