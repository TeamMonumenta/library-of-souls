package com.playmonumenta.libraryofsouls.commands;

import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.nbt.BookOfSouls;
import com.playmonumenta.libraryofsouls.nbt.EntityNBTGroups;
import com.playmonumenta.libraryofsouls.nbt.EntityNBTUtils;
import com.playmonumenta.libraryofsouls.utils.NmsUtils;
import com.playmonumenta.libraryofsouls.utils.UtilsMc;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BookOfSoulsCommand {

	private static final String COMMAND = "nbos";

	public static void register() {
		ArgumentSuggestions<CommandSender> entityTypeSuggestions = ArgumentSuggestions.strings((info) ->
			Arrays.stream(EntityType.values())
				.filter(t -> t.getEntityClass() != null)
				.map(t -> t.getKey().getKey())
				.toArray(String[]::new));

		ArgumentSuggestions<CommandSender> attributeSuggestions = ArgumentSuggestions.strings((info) ->
			Arrays.stream(Attribute.values())
				.map(a -> a.getKey().getKey())
				.toArray(String[]::new));

		// Tab-completes attributes present in the held BoS's Attributes NBT list
		ArgumentSuggestions<CommandSender> heldBosAttrSuggestions = ArgumentSuggestions.strings((info) -> {
			if (!(info.sender() instanceof Player player)) {
				return new String[0];
			}
			ItemStack item = player.getInventory().getItemInMainHand();
			if (!BookOfSouls.isValidBook(item)) {
				return new String[0];
			}
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos == null) {
				return new String[0];
			}
			var attrList = bos.getEntityNBT().getCompoundList("Attributes");
			Set<String> names = new LinkedHashSet<>();
			for (var entry : attrList) {
				String name = entry.getString("Name");
				if (name != null && !name.isEmpty()) {
					names.add(name);
				}
			}
			return names.toArray(String[]::new);
		});

		// Tab-completes NBT variable names: group keys for the entity type + already-set keys
		ArgumentSuggestions<CommandSender> varNameSuggestions = ArgumentSuggestions.strings((info) -> {
			if (!(info.sender() instanceof Player player)) {
				return new String[0];
			}
			ItemStack item = player.getInventory().getItemInMainHand();
			if (!BookOfSouls.isValidBook(item)) {
				return new String[0];
			}
			BookOfSouls bos = BookOfSouls.getFromBook(item);
			if (bos == null) {
				return new String[0];
			}
			ReadWriteNBT nbt = bos.getEntityNBT();
			EntityType type = EntityNBTUtils.getEntityType(nbt).orElse(null);
			Set<String> suggestions = new LinkedHashSet<>();
			if (type != null) {
				suggestions.addAll(EntityNBTGroups.getAllKeysForType(type));
			}
			suggestions.addAll(nbt.getKeys());
			return suggestions.toArray(String[]::new);
		});

		/* nbos get <entityType> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("get"))
			.withArguments(new StringArgument("entityType").replaceSuggestions(entityTypeSuggestions))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				String typeStr = (String) args.get(0);
				EntityType entityType = EntityNBTUtils.getEntityType(typeStr)
					.orElseThrow(() -> CommandAPI.failWithString("Unknown entity type: " + typeStr));
				ReadWriteNBT nbt = NmsUtils.getVersionAdapter().getDefaultEntityNBT(entityType);
				if (nbt == null) {
					throw CommandAPI.failWithString("Cannot create BoS: unsupported server version");
				}
				BookOfSouls.stripTransientKeys(nbt);
				BookOfSouls bos = new BookOfSouls(nbt);
				if (sender.getInventory().firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				sender.getInventory().addItem(bos.getBook());
				sender.sendMessage(Component.text("Enjoy your Book of Souls.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos getempty */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("getempty"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				if (sender.getInventory().firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				sender.getInventory().addItem(BookOfSouls.getEmpty());
				sender.sendMessage(Component.text("Enjoy your empty Book of Souls.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos attr add <attribute> <base> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("attr"))
			.withArguments(new LiteralArgument("add"))
			.withArguments(new StringArgument("attribute").replaceSuggestions(attributeSuggestions))
			.withArguments(new DoubleArgument("base"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				checkMob(bos);
				String attrName = resolveAttributeName((String) args.get(0));
				double base = (double) args.get(1);
				var attributes = bos.getEntityNBT().getCompoundList("Attributes");
				boolean found = false;
				for (var entry : attributes) {
					if (attrName.equals(entry.getString("Name"))) {
						entry.setDouble("Base", base);
						found = true;
						break;
					}
				}
				if (!found) {
					var entry = attributes.addCompound();
					entry.setString("Name", attrName);
					entry.setDouble("Base", base);
				}
				bos.saveBook();
				sender.getInventory().setItemInMainHand(bos.getBook());
				sender.sendMessage(Component.text("Entity attribute added.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos attr del <attribute> */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("attr"))
			.withArguments(new LiteralArgument("del"))
			.withArguments(new StringArgument("attribute").replaceSuggestions(heldBosAttrSuggestions))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				checkMob(bos);
				String attrName = resolveAttributeName((String) args.get(0));
				var attributes = bos.getEntityNBT().getCompoundList("Attributes");
				boolean found = false;
				for (var entry : attributes) {
					if (attrName.equals(entry.getString("Name"))) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw CommandAPI.failWithString("Entity does not have attribute " + attrName);
				}
				attributes.removeIf(e -> attrName.equals(e.getString("Name")));
				bos.saveBook();
				sender.getInventory().setItemInMainHand(bos.getBook());
				sender.sendMessage(Component.text("Entity attribute removed.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos attr delall */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("attr"))
			.withArguments(new LiteralArgument("delall"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				checkMob(bos);
				bos.getEntityNBT().removeKey("Attributes");
				bos.saveBook();
				sender.getInventory().setItemInMainHand(bos.getBook());
				sender.sendMessage(Component.text("Entity attributes cleared.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos var <name> — get current value */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("var"))
			.withArguments(new StringArgument("name").replaceSuggestions(varNameSuggestions))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				String name = (String) args.get(0);
				ReadWriteNBT nbt = bos.getEntityNBT();
				if (!nbt.hasTag(name)) {
					sender.sendMessage(Component.text("Variable '" + name + "' is not set.", NamedTextColor.RED));
					return;
				}
				sender.sendMessage(Component.text(name + ": " + BookOfSouls.formatNbtValue(nbt, name), NamedTextColor.AQUA));
			})
			.register();

		/* nbos var <name> <value> — set via SNBT */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("var"))
			.withArguments(new StringArgument("name").replaceSuggestions(varNameSuggestions))
			.withArguments(new GreedyStringArgument("value"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				String name = (String) args.get(0);
				String value = (String) args.get(1);
				ReadWriteNBT parsed;
				try {
					parsed = NBT.parseNBT("{" + name + ":" + value + "}");
				} catch (Exception e) {
					throw CommandAPI.failWithString("Invalid SNBT value: " + e.getMessage());
				}
				bos.getEntityNBT().mergeCompound(parsed);
				bos.saveBook();
				sender.getInventory().setItemInMainHand(bos.getBook());
				sender.sendMessage(Component.text("Variable '" + name + "' set.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos refresh — re-render book pages from current entity NBT */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("refresh"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				bos.saveBook(false);
				sender.getInventory().setItemInMainHand(bos.getBook());
				sender.sendMessage(Component.text("Book of Souls refreshed.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos tocommand — write /summon command to targeted command block */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("tocommand"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				EntityType entityType = EntityNBTUtils.getEntityType(bos.getEntityNBT())
					.orElseThrow(() -> CommandAPI.failWithString("Book of Souls has no entity type!"));
				BlockState state = UtilsMc.getTargetBlock(sender, 5).getState();
				if (!(state instanceof CommandBlock cmdBlock)) {
					throw CommandAPI.failWithString("No Command Block in sight!");
				}
				ReadWriteNBT nbtCopy = NBT.parseNBT(bos.getEntityNBT().toString());
				nbtCopy.removeKey("id");
				String command = "/summon " + entityType.getKey().asMinimalString() + " ~ ~1 ~ " + nbtCopy.toString();
				if (command.length() > 32767 - 50) {
					throw CommandAPI.failWithString("Entity too complex!");
				}
				cmdBlock.setCommand(command);
				state.update();
				sender.sendMessage(Component.text("Command set.", NamedTextColor.GREEN));
			})
			.register();

		/* nbos debug — NBT group coverage report */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("debug"))
			.executes((sender, args) -> {
				runNbtDebug(sender);
			})
			.register();

		/* nbos toegg — create spawn egg from held BoS */
		new CommandAPICommand(COMMAND)
			.withPermission(CommandPermission.OP)
			.withArguments(new LiteralArgument("toegg"))
			.executesPlayer((sender, args) -> {
				checkCreative(sender);
				BookOfSouls bos = getBos(sender);
				EntityType entityType = EntityNBTUtils.getEntityType(bos.getEntityNBT())
					.orElseThrow(() -> CommandAPI.failWithString("Book of Souls has no entity type!"));
				sender.sendMessage(Component.text("Some entities may not spawn from eggs.", NamedTextColor.YELLOW));
				Material eggMaterial;
				try {
					eggMaterial = Material.valueOf(entityType.name() + "_SPAWN_EGG");
				} catch (IllegalArgumentException e) {
					eggMaterial = Material.TURTLE_SPAWN_EGG;
				}
				ItemStack egg = UtilsMc.newSingleItemStack(eggMaterial,
					Component.text("Spawn Egg - " + entityType.getKey().asMinimalString()));
				final ReadWriteNBT entityNbt = bos.getEntityNBT();
				NBT.modify(egg, nbt -> {
					nbt.getOrCreateCompound("EntityTag").mergeCompound(entityNbt);
				});
				if (sender.getInventory().firstEmpty() == -1) {
					throw CommandAPI.failWithString("Your inventory is full!");
				}
				sender.getInventory().addItem(egg);
				sender.sendMessage(Component.text("Egg created.", NamedTextColor.GREEN));
			})
			.register();
	}

	private static void checkCreative(Player player) throws WrapperCommandSyntaxException {
		if (player.getGameMode() != GameMode.CREATIVE) {
			throw CommandAPI.failWithString("Must be in creative mode!");
		}
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

	private static void checkMob(BookOfSouls bos) throws WrapperCommandSyntaxException {
		EntityType entityType = EntityNBTUtils.getEntityType(bos.getEntityNBT()).orElse(null);
		Class<?> entityClass = entityType != null ? entityType.getEntityClass() : null;
		if (entityClass == null || !Mob.class.isAssignableFrom(entityClass)) {
			throw CommandAPI.failWithString("That must be a Mob entity!");
		}
	}

	// Accepts either full key ("minecraft:generic.max_health") or short form ("generic.max_health")
	private static String resolveAttributeName(String input) {
		for (Attribute attr : Attribute.values()) {
			if (attr.getKey().asString().equals(input) || attr.getKey().getKey().equals(input)) {
				return attr.getKey().asString();
			}
		}
		return input;
	}

	private static void runNbtDebug(CommandSender sender) {
		// Keys that are intentionally excluded from all groups (not coverage gaps)
		Set<String> noise = Set.of("id", "Passengers");

		// Section 1: unhandled keys per entity type
		// Section 2: entity types with no specific group (only Entity/LivingEntity/Mob)
		// Section 3: group keys that appear in no entity's default NBT
		Set<String> generalGroupNames = Set.of("Entity", "LivingEntity", "Mob");
		// Collect every key defined across all groups (used for stale-key detection)
		Set<String> allGroupKeys = new TreeSet<>();
		for (EntityType type : EntityType.values()) {
			for (EntityNBTGroups.Group g : EntityNBTGroups.getGroupsForType(type)) {
				allGroupKeys.addAll(g.nbtKeys());
			}
		}

		TreeMap<String, List<String>> unhandledByType = new TreeMap<>();
		List<String> noSpecificGroup = new ArrayList<>();
		Set<String> seenDefaultKeys = new TreeSet<>();

		for (EntityType type : EntityType.values()) {
			if (type.getEntityClass() == null) {
				continue;
			}
			Set<String> defaultKeys;
			try {
				defaultKeys = NmsUtils.getVersionAdapter().getDefaultEntityNBTKeys(type);
			} catch (Exception e) {
				LibraryOfSouls.getInstance().getLogger().warning("nbt-debug: failed to get keys for " + type.getKey() + ": " + e.getMessage());
				continue;
			}
			if (defaultKeys.isEmpty()) {
				continue;
			}
			seenDefaultKeys.addAll(defaultKeys);

			Set<String> typeGroupKeys = EntityNBTGroups.getAllKeysForType(type);
			List<String> unhandled = new ArrayList<>();
			for (String key : new TreeSet<>(defaultKeys)) {
				if (!typeGroupKeys.contains(key) && !noise.contains(key)) {
					unhandled.add(key);
				}
			}
			if (!unhandled.isEmpty()) {
				unhandledByType.put(type.getKey().asMinimalString(), unhandled);
			}

			List<EntityNBTGroups.Group> groups = EntityNBTGroups.getGroupsForType(type);
			boolean isMob = groups.stream().anyMatch(g -> g.displayName().equals("Mob"));
			boolean hasSpecificGroup = groups.stream().anyMatch(g -> !generalGroupNames.contains(g.displayName()));
			if (isMob && !hasSpecificGroup) {
				noSpecificGroup.add(type.getKey().asMinimalString());
			}
		}

		List<String> staleKeys = new ArrayList<>();
		for (String key : allGroupKeys) {
			if (!seenDefaultKeys.contains(key)) {
				staleKeys.add(key);
			}
		}

		var log = LibraryOfSouls.getInstance().getLogger();
		log.info("=== NBT Debug Report ===");
		log.info("");
		log.info("## Unhandled Keys (potential group coverage gaps)");
		if (unhandledByType.isEmpty()) {
			log.info("(none)");
		} else {
			for (Map.Entry<String, List<String>> entry : unhandledByType.entrySet()) {
				log.info("  " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
			}
		}
		log.info("");
		log.info("## Entity Types with No Specific Group");
		if (noSpecificGroup.isEmpty()) {
			log.info("(none)");
		} else {
			for (String typeName : noSpecificGroup) {
				log.info("  " + typeName);
			}
		}
		log.info("");
		log.info("## Stale/Optional Group Keys (not in any entity's default NBT)");
		if (staleKeys.isEmpty()) {
			log.info("(none)");
		} else {
			log.info("  " + String.join(", ", staleKeys));
		}
		log.info("=== End NBT Debug Report ===");

		int gapCount = unhandledByType.values().stream().mapToInt(List::size).sum();
		sender.sendMessage(Component.text(
			"NBT debug report written to server log. "
			+ gapCount + " unhandled key(s) across " + unhandledByType.size() + " type(s), "
			+ noSpecificGroup.size() + " type(s) with no specific group, "
			+ staleKeys.size() + " stale/optional key(s)."));
	}
}
