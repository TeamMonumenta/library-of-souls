package com.playmonumenta.libraryofsouls.nbt;

import com.playmonumenta.libraryofsouls.nbt.types.EntityNBTGroups;
import com.playmonumenta.libraryofsouls.nbt.types.EntityNBTGroups.Group;
import com.playmonumenta.libraryofsouls.nbt.types.EntityNBTGroups.NbtField;
import com.playmonumenta.libraryofsouls.nbt.types.NbtFieldType;
import com.playmonumenta.libraryofsouls.utils.Utils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class BookOfSouls {

	private static final String _author = ChatColor.GOLD + "The Creator";
	private static final String _dataTitle = "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Soul Data v0.2" + ChatColor.BLACK + "\n";
	private static final String _dataTitleOLD = "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Soul Data v0.1" + ChatColor.BLACK + "\n";
	private static final String mKey = "Book of Souls";
	private static final Component mTitle = Component.text(mKey, NamedTextColor.AQUA);
	private static final Component mEmptyTitle = Component.text(mKey, NamedTextColor.GREEN).append(Component.text(" - ", Style.empty())).append(Component.text("Empty", NamedTextColor.RED));
	private static final List<Component> mEmptyLore = List.of(
		Component.text("This is a empty Book of Souls.", NamedTextColor.AQUA),
		Component.text("Right-click an entity to capture the soul.", NamedTextColor.AQUA)
	);

	private static final List<String> TRANSIENT_KEYS = List.of(
		"Pos", "Motion", "Rotation", "UUID", "FallDistance", "Fire", "Air",
		"OnGround", "PortalCooldown", "WorldUUIDLeast", "WorldUUIDMost");

	private static Plugin _plugin = null;

	private ItemStack _book;
	private ReadWriteNBT _entityNbt;

	public static void initialize(Plugin plugin) {
		if (_plugin != null) {
			return;
		}
		_plugin = plugin;
	}

	public static ReadWriteNBT bookToEntityNBT(ItemStack book) {
		if (isValidBook(book)) {
			try {
				final var newData = NBT.get(book, nbt -> {
					return nbt.getByteArray("BOS");
				});
				if (newData != null) {
					final var mojangson = new String(newData, StandardCharsets.UTF_8);
					// TODO: run it through DFU?
					return NBT.parseNBT(mojangson);
				}

				// Legacy serialization
				String data = LegacyBookSerialize.loadData((BookMeta) book.getItemMeta(), _dataTitle);
				if (data != null) {
					if (data.startsWith("§k")) {
						// Dirty fix, for some reason 'data' can sometimes start with §k.
						// Remove it!!!
						data = data.substring(2);
					}

					// decode it
					final var decoded = Base64.getDecoder().decode(data);
					try (var in = new ByteArrayInputStream(decoded)) {
						return NBT.readNBT(in);
					}
				}
			} catch (Exception e) {
				_plugin.getLogger().log(Level.WARNING, "Corrupt Book of Souls.", e);
				return null;
			}
		}
		return null;
	}

	public static BookOfSouls getFromBook(ItemStack book) {
		ReadWriteNBT entityNbt = bookToEntityNBT(book);
		if (entityNbt != null) {
			return new BookOfSouls(book, entityNbt);
		}
		return null;
	}

	public static ItemStack getEmpty() {
		final var item = new ItemStack(Material.BOOK);
		final var meta = item.getItemMeta();
		if (meta instanceof BookMeta bookMeta) {
			bookMeta.title(mEmptyTitle);
			item.setItemMeta(bookMeta);
		}
		item.lore(mEmptyLore);
		return item;

	}

	/** Strips transient entity keys (position, UUID, etc.) from captured entity NBT in-place. */
	public static ReadWriteNBT stripTransientKeys(ReadWriteNBT nbt) {
		for (String key : TRANSIENT_KEYS) {
			nbt.removeKey(key);
		}
		ReadWriteNBT paper = nbt.getCompound("Paper");
		if (paper != null) {
			paper.removeKey("Origin");
			paper.removeKey("FromMobSpawner");
		}
		return nbt;
	}

	private static ReadWriteNBT fromSnapshot(EntitySnapshot snapshot) {
		return stripTransientKeys(EntityNBTUtils.getNBTFromEntitySnapshot(snapshot));
	}

	public BookOfSouls(final Entity entity) {
		this(null, entity.createSnapshot());
	}

	public BookOfSouls(final EntitySnapshot snapshot) {
		this(null, fromSnapshot(snapshot));
	}

	public BookOfSouls(ReadWriteNBT entityNBT) {
		this(null, entityNBT);
	}

	public BookOfSouls(ItemStack book, final Entity entity) {
		this(book, entity.createSnapshot());
	}

	public BookOfSouls(ItemStack book, final EntitySnapshot snapshot) {
		this(book, fromSnapshot(snapshot));
	}

	private BookOfSouls(ItemStack book, ReadWriteNBT entityNBT) {
		_book = book;
		_entityNbt = entityNBT;
	}

	public static boolean isValidBook(ItemStack book) {
		if (book == null) {
			return false;
		}
		if (book.getType() != Material.WRITTEN_BOOK) {
			return false;
		}
		ItemMeta meta = book.getItemMeta();
		if (meta instanceof BookMeta bookMeta) {
			if (bookMeta.hasTitle() && mKey.equals(bookMeta.getTitle())) {
				return true;
			}
		}
		return NBT.get(book, nbt -> {
			return nbt.hasTag("BOS");
		});
	}

	public static boolean isValidEmptyBook(ItemStack book) {
		if (book == null) {
			return false;
		}
		if (book.getType() != Material.BOOK) {
			return false;
		}
		String mmDisplayName = Utils.MINIMESSAGE.serialize(book.displayName());
		// TODO: massive hack, better way to compare Components is serializing to minimessage and comparing those strings
		return mmDisplayName.contains("Book of Souls") && mmDisplayName.contains("Empty");
	}

	public ReadWriteNBT getEntityNBT() {
		return _entityNbt;
	}

	public void saveEntityNBT(Entity entity) {
		final var snapshot = entity.createSnapshot();
		final var newNBT = EntityNBTUtils.getNBTFromEntitySnapshot(snapshot);
		_entityNbt = newNBT;
	}

	public ItemStack getBook() {
		if (_book == null) {
			_book = new ItemStack(Material.WRITTEN_BOOK);
			saveBook(true);
		}
		return _book;
	}

	public void saveBook() {
		saveBook(false);
	}

	public void saveBook(boolean resetName) {
		BookMeta meta = (BookMeta) _book.getItemMeta();
		EntityType entityType = EntityNBTUtils.getEntityType(_entityNbt).orElse(null);
		String entityName = entityType != null ? entityType.getKey().asMinimalString() : "unknown";

		if (resetName) {
			meta.displayName(Component.empty().append(mTitle).append(Component.text(" - ", Style.empty())).append(Component.text(entityName, NamedTextColor.RED)));
			meta.title(mTitle);
			meta.setAuthor(_author);
		}

		meta.setPages(new ArrayList<String>());

		StringBuilder sb = new StringBuilder();
		sb.append("Soul: " + ChatColor.RED + ChatColor.BOLD + entityName + "\n\n");
		int x = 7;

		List<Group> groups = entityType != null ? EntityNBTGroups.getGroupsForType(entityType) : List.of();
		Map<String, NbtFieldType> fieldTypeMap = entityType != null
			? EntityNBTGroups.getFieldTypeMap(entityType)
			: Map.of();

		for (Group group : groups) {
			boolean hasAny = group.fields().stream().map(NbtField::key).anyMatch(_entityNbt::hasTag);
			if (!hasAny) {
				continue;
			}
			if (x == 1) {
				meta.addPage(sb.toString());
				sb = new StringBuilder();
				x = 11;
			}
			sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + group.displayName() + ":\n");
			for (NbtField field : group.fields()) {
				if (!_entityNbt.hasTag(field.key())) {
					continue;
				}
				if (--x == 0) {
					meta.addPage(sb.toString());
					sb = new StringBuilder();
					x = 10;
				}
				String formatted = legacyFormat(field.type(), _entityNbt, field.key());
				sb.append("  " + ChatColor.DARK_BLUE + field.key() + ": " + ChatColor.BLACK + formatted + "\n");
			}
		}

		// Other: keys not covered by any group (except Attributes which gets its own section)
		Set<String> otherKeys = new LinkedHashSet<>(_entityNbt.getKeys());
		otherKeys.removeAll(fieldTypeMap.keySet());
		otherKeys.remove("Attributes");
		if (!otherKeys.isEmpty()) {
			if (x == 1) {
				meta.addPage(sb.toString());
				sb = new StringBuilder();
				x = 11;
			}
			sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "Other:\n");
			for (String key : otherKeys) {
				if (--x == 0) {
					meta.addPage(sb.toString());
					sb = new StringBuilder();
					x = 10;
				}
				String formatted = legacyFormat(NbtFieldType.generic(), _entityNbt, key);
				sb.append("  " + ChatColor.DARK_BLUE + key + ": " + ChatColor.BLACK + formatted + "\n");
			}
		}
		meta.addPage(sb.toString());

		// Attributes section
		var attributesList = _entityNbt.getCompoundList("Attributes");
		if (!attributesList.isEmpty()) {
			sb = new StringBuilder();
			sb.append("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Attributes:\n");
			x = 11;
			for (ReadWriteNBT attr : attributesList) {
				if (x <= 3) {
					meta.addPage(sb.toString());
					sb = new StringBuilder();
					x = 11;
				}
				sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + attr.getString("Name") + ":\n");
				sb.append("  " + ChatColor.DARK_BLUE + "Base: " + ChatColor.BLACK + attr.getDouble("Base") + "\n");
				sb.append("  " + ChatColor.DARK_BLUE + "Modifiers:\n");
				x -= 3;
				var modifiers = attr.getCompoundList("Modifiers");
				if (modifiers.isEmpty()) {
					sb.append("    " + ChatColor.BLACK + "" + ChatColor.ITALIC + "none\n");
					x--;
				} else {
					for (ReadWriteNBT mod : modifiers) {
						if (x <= 2) {
							meta.addPage(sb.toString());
							sb = new StringBuilder();
							x = 11;
						}
						sb.append("    " + ChatColor.RED + mod.getString("Name") + ChatColor.DARK_GREEN + " Op: " + ChatColor.BLACK + mod.getInteger("Operation") + "\n");
						sb.append("      " + ChatColor.DARK_GREEN + "Amount: " + ChatColor.BLACK + mod.getDouble("Amount") + "\n");
						x -= 2;
					}
				}
				sb.append("\n");
				x--;
			}
			meta.addPage(sb.toString());
		}

		// LegacyBookSerialize.saveToBook(meta, _entityNbt.serialize(), _dataTitle);
		meta.addPage("RandomId: " + Integer.toHexString((new Random()).nextInt()) + "\n\n\n"
				+ ChatColor.DARK_BLUE + ChatColor.BOLD + "      The END.");
		_book.setItemMeta(meta);
		final var mojangson = _entityNbt.toString();
		NBT.modify(_book, nbt -> {
			nbt.setByteArray("BOS", mojangson.getBytes(StandardCharsets.UTF_8));
		});
	}

	/** Shim: formats a field value as a legacy ChatColor string for use in book pages. */
	private static String legacyFormat(NbtFieldType type, ReadableNBT nbt, String key) {
		Component component = type.formatForBook(nbt, key);
		String s = LegacyComponentSerializer.legacySection().serialize(component);
		if (s.length() > 60) {
			s = s.substring(0, 59) + "\u2026";
		}
		return s;
	}
}
