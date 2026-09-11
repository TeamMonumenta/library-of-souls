package com.playmonumenta.libraryofsouls.commands;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.Attribute;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeContainer;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeType;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.NBTVariable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import it.unimi.dsi.fastutil.doubles.DoubleDoublePair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE;

public class LibraryOfSoulsCommand {
	/* Several sub commands have this same tab completion */
	public static final ArgumentSuggestions<CommandSender> LIST_MOBS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_PARTIES_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPartyNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_POOLS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulPoolNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_SOUL_GROUPS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listSoulGroupNames().toArray(String[]::new));
	public static final ArgumentSuggestions<CommandSender> LIST_AUTHORS_FUNCTION = ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listAuthors().toArray(String[]::new));
	private static final String COMMAND = "los";
	private static final Pattern VALID_SOUL_GROUP_LABEL = Pattern.compile("[0-9A-Za-z_]+");

	// No clue why these are scoreholder arguments, but not going to take the risk of changing them

	public static void register() {
		LocationArgument locationArg = new LocationArgument("location");
		LocationArgument blockLocationArg = new LocationArgument("location", LocationType.BLOCK_POSITION);
		LocationArgument pos1Arg = new LocationArgument("pos1");
		LocationArgument pos2Arg = new LocationArgument("pos2");
		Argument<String> areaArg = new StringArgument("area").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobLocations().toArray(String[]::new)));
		Argument<String> idArg = new StringArgument("id").replaceSuggestions(ArgumentSuggestions.strings((info) -> SoulsDatabase.getInstance().listMobTypes().toArray(String[]::new)));
		Argument<String> authorArg = new StringArgument("author").replaceSuggestions(LIST_AUTHORS_FUNCTION);

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
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				PlayerInventory inv = getPlayer(sender).getInventory();
				if (inv.firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				inv.addItem(getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))).getBoS());
			})
			.register();

		/* los party <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.party"))
			.withArguments(new LiteralArgument("party"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.executes((sender, args) -> {
				String partyLabel = args.getByArgument(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION));
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
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
			.executes((sender, args) -> {
				String poolLabel = args.getByArgument(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION));
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
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.executes((sender, args) -> {
				String groupLabel = args.getByArgument(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION));
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
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				new SoulsInventory(player, getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))).getHistory(), "History")
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los summon <location> <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summon"))
			.withArguments(new LiteralArgument("summon"))
			.withArguments(locationArg)
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				getSoul(args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))).summon(args.getByArgument(locationArg));
			})
			.register();

		/* los summongroup <name> <pos1> <pos2> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.summongroup"))
			.withArguments(new LiteralArgument("summongroup"))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(pos1Arg)
			.withArguments(pos2Arg)
			.executes((sender, args) -> {
				Location pos1 = args.getByArgument(pos1Arg);
				Location pos2 = args.getByArgument(pos2Arg);
				BoundingBox bb = BoundingBox.of(pos1, pos2);
				getSoulGroup(args.getByArgument(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))).summonGroup(new Random(), pos1.getWorld(), bb);
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

		/* los searchauthor <author> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.search"))
			.withArguments(new LiteralArgument("searchauthor"))
			.withArguments(authorArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String author = args.getByArgument(authorArg);
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByAuthor(author);
				if (souls == null) {
					throw CommandAPI.failWithString("Mobs authored by '" + author + "' not found");
				}
				new SoulsInventory(player, souls, author)
					.openInventory(player, LibraryOfSouls.getInstance());
			})
			.register();

		/* los spawner <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.spawner"))
			.withArguments(new LiteralArgument("spawner"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String name = args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION));
				Soul soul = SoulsDatabase.getInstance().getSoul(name);
				if (soul == null) {
					throw CommandAPI.failWithString("Soul '" + name + "' not found");
				}
				SpawnerInventory.openSpawnerInventory(soul, player, null);
			})
			.register();

		/* los search <area> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.getarea"))
			.withArguments(new LiteralArgument("getarea"))
			.withArguments(blockLocationArg)
			.withArguments(areaArg)
			.executes((sender, args) -> {
				String area = args.getByArgument(areaArg);
				List<SoulEntry> souls = SoulsDatabase.getInstance().getSoulsByLocation(area);
				if (souls == null) {
					throw CommandAPI.failWithString("Area '" + area + "' not found");
				}
				Location loc = args.getByArgument(blockLocationArg);
				if (loc == null) {
					return;
				}
				int start = 0;
				while (start < souls.size()) {
					while (!loc.getBlock().isEmpty()) {
						loc = loc.add(1, 0, 0);
					}
					Block block = loc.getBlock();
					block.setType(Material.CHEST);
					Chest state = (Chest) block.getState();
					Inventory inventory = state.getBlockInventory();

					for (int i = start; i < Math.min(souls.size(), start + 27); i++) {
						SoulEntry soulEntry = souls.get(i);
						inventory.setItem(i - start, soulEntry.getBoS());
					}
					start += 27;
				}
			})
			.register();

		for (SoulModifier soulModifier : SoulModifier.values()) {
			new CommandAPICommand(COMMAND)
				.withPermission(CommandPermission.fromString("los.massedit"))
				.withArguments(new LiteralArgument("massedit"))
				.withArguments(blockLocationArg, new LiteralArgument(soulModifier.name().toLowerCase(Locale.ROOT)), new DoubleArgument("multiplier", 0))
				.executesPlayer((player, args) -> {
					Location location = args.getByArgument(blockLocationArg);
					if (location == null) {
						throw CommandAPI.failWithString("No location provided");
					}
					Block block = location.getBlock();
					if (!(block.getState() instanceof Chest chest)) {
						throw CommandAPI.failWithString("Not a chest!");
					}
					Inventory inventory = chest.getBlockInventory();
					double multiplier = args.getOrDefaultUnchecked("multiplier", 1.0);
					List<Component> output = new ArrayList<>(inventory.getSize());
					@Nullable ItemStack[] contents = inventory.getContents();
					for (int i = 0; i < contents.length; i++) {
						ItemStack item = inventory.getItem(i);
						if (item == null) {
							continue;
						}
						BookOfSouls bos = getBos(item);
						DoubleDoublePair result = soulModifier.modifySoul(bos, multiplier, player);

						bos.saveBook();
						ItemStack book = bos.getBook();
						inventory.setItem(i, book);

						EntityNBT nbt = bos.getEntityNBT();
						@Nullable
						NBTVariable nameVar = nbt.getVariable("Name");
						Component name = nameVar != null
							? GsonComponentSerializer.gson().deserialize(nameVar.get())
							: Component.text(nbt.getId());
						if (result != null) {
							output.add(Component.empty()
								.append(name)
								.append(Component.text(": %s → %s".formatted(result.firstDouble(), result.secondDouble()))));
						} else {
							output.add(Component.empty()
								.append(name)
								.append(Component.text(": Skipped!", NamedTextColor.RED)));
						}
					}
					if (output.isEmpty()) {
						return;
					}
					player.sendMessage(Component.text("Changed souls inside this chest: ", NamedTextColor.GOLD));
					player.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), output));
					player.sendMessage(Component.text("[Update all souls]", NamedTextColor.GOLD, TextDecoration.BOLD)
						.clickEvent(ClickEvent.runCommand("los massupdate %d %d %d".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ())))
						.hoverEvent(Component.text("WARNING: This will UPDATE all the souls inside the chest!"))
					);
				})
				.register();
		}
	}

	public enum SoulModifier {
		HEALTH((book, multiplier, player) -> {
			EntityNBT nbt = book.getEntityNBT();
			if (!(nbt instanceof MobNBT mobNBT)) {
				return null;
			}
			NBTVariable healthVar = nbt.getVariable("Health");
			if (healthVar == null) {
				return null;
			}
			String healthString = healthVar.get();
			if (healthString == null) {
				return null;
			}
			double originalHealth = Double.parseDouble(healthString);
			double health = originalHealth * multiplier;
			// Heuristic
			if (health < 5) {
				return null;
			}
			if (health > 40.0) {
				double rem = health % (double) 5;
				if (rem < 2.5) {
					health = health - rem;
				} else {
					health = health - rem + 5;
				}
			} else {
				health = Math.round(health);
			}
			healthVar.set(String.valueOf(health), player);
			AttributeContainer attributes = mobNBT.getAttributes();
			Attribute attribute = attributes.getAttribute(AttributeType.MAX_HEALTH);
			double maxHealth = attribute.getBase();
			maxHealth *= multiplier;
			if (maxHealth > 5) {
				maxHealth = maxHealth - (maxHealth % 5);
			}
			attribute.setBase(maxHealth);
			mobNBT.setAttributes(attributes);
			return DoubleDoublePair.of(originalHealth, health);
		}),
		ATTACK_DAMAGE((book, multiplier, player) -> {
			EntityNBT nbt = book.getEntityNBT();
			NBTVariable handItems = nbt.getVariable("HandItems");
			if (!(handItems instanceof ItemsVariable itemsVariable)) {
				return null;
			}
			ItemStack[] items = itemsVariable.getItems();
			@Nullable DoubleDoublePair res = null;
			ItemStack mainhand = items[0];
			if (mainhand != null) {
				ItemMeta meta = mainhand.getItemMeta();
				Multimap<org.bukkit.attribute.Attribute, AttributeModifier> existing = meta.getAttributeModifiers();
				Collection<AttributeModifier> attackAttribute = meta.getAttributeModifiers(GENERIC_ATTACK_DAMAGE);
				@Nullable
				DoubleDoublePair ans = null;
				if (existing != null && attackAttribute != null && !attackAttribute.isEmpty()) {
					List<AttributeModifier> mutAttributes = new ArrayList<>(attackAttribute);
					ListIterator<AttributeModifier> iter = mutAttributes.listIterator();
					while (iter.hasNext()) {
						AttributeModifier modifier = iter.next();
						if (modifier.getOperation() != AttributeModifier.Operation.ADD_NUMBER) {
							continue;
						}
						double originalModifier = modifier.getAmount();
						double newModifier = Math.round(originalModifier * multiplier);
						iter.set(new AttributeModifier(modifier.getUniqueId(), modifier.getName(), newModifier, modifier.getOperation(), modifier.getSlot()));
						ans = DoubleDoublePair.of(originalModifier, newModifier);
					}
					existing = HashMultimap.create(existing);
					existing.replaceValues(GENERIC_ATTACK_DAMAGE, mutAttributes);
					mainhand.setItemMeta(meta);
					meta.setAttributeModifiers(existing);
					items[1] = mainhand;
					itemsVariable.setItems(items);
				}
				res = ans;
			}
			if (res != null) {
				return res;
			}

			if (!(nbt instanceof MobNBT mobNBT)) {
				return null;
			}
			AttributeContainer attributes = mobNBT.getAttributes();
			@Nullable
			Attribute attribute = attributes.getAttribute(AttributeType.ATTACK_DAMAGE);
			if (attribute == null) {
				return null;
			}
			double originalAttack = attribute.getBase();
			double attack = Math.round(originalAttack * multiplier);
			attribute.setBase(attack);
			mobNBT.setAttributes(attributes);
			return DoubleDoublePair.of(originalAttack, attack);
		}),
		BOW_POWER((book, multiplier, player) -> {
			EntityNBT nbt = book.getEntityNBT();
			NBTVariable handItems = nbt.getVariable("HandItems");
			if (!(handItems instanceof ItemsVariable itemsVariable)) {
				return null;
			}
			ItemStack[] items = itemsVariable.getItems();
			ItemStack mainhand = items[0];
			if (mainhand == null) {
				return null;
			}
			ItemMeta meta = mainhand.getItemMeta();
			int originalPower = meta.getEnchantLevel(Enchantment.ARROW_DAMAGE);
			int power = (int) Math.round(originalPower * multiplier);
			if (power == 0) {
				return null;
			}
			meta.addEnchant(Enchantment.ARROW_DAMAGE, power, true);
			items[0] = mainhand;
			itemsVariable.setItems(items);
			return DoubleDoublePair.of(originalPower, power);
		}),
		;

		@FunctionalInterface
		private interface ModifierInterface {
			@Nullable
			DoubleDoublePair modify(BookOfSouls book, double multiplier, Player player);
		}

		private final ModifierInterface mSoulModifier;

		SoulModifier(ModifierInterface soulModifier) {
			mSoulModifier = soulModifier;
		}

		public @Nullable DoubleDoublePair modifySoul(BookOfSouls bos, double multiplier, Player player) {
			return mSoulModifier.modify(bos, multiplier, player);
		}
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

		/* los massupdate */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.update"))
			.withArguments(new LiteralArgument("massupdate"))
			.withArguments(new LocationArgument("location", LocationType.BLOCK_POSITION))
			.executes((sender, args) -> {
				Location location = args.getUnchecked("location");
				if (location == null) {
					throw CommandAPI.failWithString("No location provided");
				}
				Block block = location.getBlock();
				if (!(block.getState() instanceof Chest chest)) {
					throw CommandAPI.failWithString("Not a chest!");
				}
				Player player = getPlayer(sender);
				Inventory inventory = chest.getBlockInventory();
				ArrayList<BookOfSouls> souls = new ArrayList<>(inventory.getSize());
				for (ItemStack item : inventory) {
					if (item == null) {
						continue;
					}
					if (BookOfSouls.isValidBook(item)) {
						BookOfSouls bos = getBos(item);
						souls.add(bos);
					}
				}
				SoulsDatabase.getInstance().update(player, souls.toArray(new BookOfSouls[0]));
			})
			.register();

		/* los del <name> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.del"))
			.withArguments(new LiteralArgument("del"))
			.withArguments(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION))
			.executes((sender, args) -> {
				SoulsDatabase.getInstance().del(sender, args.getByArgument(new StringArgument("mobLabel").replaceSuggestions(LIST_MOBS_FUNCTION)));
			})
			.register();

		/* los addparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addparty"))
			.withArguments(new LiteralArgument("addparty"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION).replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = args.getByArgument(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION));
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
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(countArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String partyLabel = args.getByArgument(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION));
				String entryLabel = args.getByArgument(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION));
				int count = args.getByArgument(countArg);

				SoulsDatabase.getInstance().updateParty(player, partyLabel, entryLabel, count);
			})
			.register();

		/* los delparty <partyLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delparty"))
			.withArguments(new LiteralArgument("delparty"))
			.withArguments(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION))
			.executes((sender, args) -> {
				String partyLabel = args.getByArgument(new ScoreHolderArgument.Single("partyLabel").replaceSuggestions(LIST_SOUL_PARTIES_FUNCTION));
				SoulsDatabase.getInstance().delParty(sender, partyLabel);
			})
			.register();

		/* los addpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.addpool"))
			.withArguments(new LiteralArgument("addpool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION).replaceSuggestions(ArgumentSuggestions.empty()))
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = args.getByArgument(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION));
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
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
			.withArguments(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION))
			.withArguments(weightArg)
			.executes((sender, args) -> {
				Player player = getPlayer(sender);
				String poolLabel = args.getByArgument(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION));
				String entryLabel = args.getByArgument(new ScoreHolderArgument.Single("groupLabel").replaceSuggestions(LIST_SOUL_GROUPS_FUNCTION));
				int count = args.getByArgument(weightArg);

				SoulsDatabase.getInstance().updatePool(player, poolLabel, entryLabel, count);
			})
			.register();

		/* los delpool <poolLabel> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.fromString("los.delpool"))
			.withArguments(new LiteralArgument("delpool"))
			.withArguments(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION))
			.executes((sender, args) -> {
				String poolLabel = args.getByArgument(new ScoreHolderArgument.Single("poolLabel").replaceSuggestions(LIST_SOUL_POOLS_FUNCTION));
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
		if (sender instanceof Player player) {
			return player;
		} else if (sender instanceof ProxiedCommandSender proxiedCommandSender && proxiedCommandSender.getCallee() instanceof Player player) {
			return player;
		}

		throw CommandAPI.failWithString("This command must be run by / as a player");
	}

	private static BookOfSouls getBos(Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		return getBos(item);
	}

	private static BookOfSouls getBos(ItemStack item) throws WrapperCommandSyntaxException {
		if (BookOfSouls.isValidBook(item)) {
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos != null) {
				return bos;
			}
		}
		throw CommandAPI.failWithString("That Book of Souls is corrupted!");
	}
}
