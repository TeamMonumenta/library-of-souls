package com.playmonumenta.libraryofsouls.nbt;

import com.playmonumenta.libraryofsouls.utils.Utils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import net.iharder.Base64;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
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

	private static Plugin _plugin = null;

	private ItemStack _book;
	private ReadWriteNBT _entityNbt;

	public static void initialize(Plugin plugin) {
		if (_plugin != null) return;
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
					final var decoded = Base64.decode(data);
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

	public BookOfSouls(final Entity entity) {
		this(null, entity.createSnapshot());
	}

	public BookOfSouls(final EntitySnapshot snapshot) {
		this(null, EntityNBTUtils.getNBTFromEntitySnapshot(snapshot));
	}

	public BookOfSouls(ReadWriteNBT entityNBT) {
		this(null, entityNBT);
	}

	public BookOfSouls(ItemStack book, final Entity entity) {
		this(book, entity.createSnapshot());
	}

	public BookOfSouls(ItemStack book, final EntitySnapshot snapshot) {
		this(book, EntityNBTUtils.getNBTFromEntitySnapshot(snapshot));
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
		final var entity = EntityNBTUtils.getFakeEntity(_entityNbt);
		String entityName = entity.getType().getKey().asMinimalString();

		if (resetName) {
			meta.displayName(Component.empty().append(mTitle).append(Component.text(" - ", Style.empty())).append(Component.text(entityName, NamedTextColor.RED)));
			meta.title(mTitle);
			meta.setAuthor(_author);
		}

		meta.setPages(new ArrayList<String>());

		/**
			* TODO: Maybe a way of recursively displaying all of the values you can get via paper api (flowey probably has a better way of doing this)
		*/
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("This book contains the soul of a " + ChatColor.RED + ChatColor.BOLD + entityName + "\n\n");

		int x = 7;
		if (entity instanceof MinecartSpawnerNBT) {
			sb.append(ChatColor.BLACK + "Left-click a existing spawner to copy the entities and variables from the spawner, left-click while sneaking to copy them back to the spawner.");
			meta.addPage(sb.toString());
			sb = new StringBuilder();
			x = 11;
		} else if (_entityNbt instanceof FallingBlockNBT) {
			sb.append(ChatColor.BLACK + "Left-click a block while sneaking to copy block data.\n\n");
			x = 5;
		}

		for (NBTVariableContainer container : _entityNbt.getAllVariables()) {
			if (x == 1) {
				meta.addPage(sb.toString());
				sb = new StringBuilder();
				x = 11;
			}
			sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + container.getName() + ":\n");
			for (String name : container.getVariableNames()) {
				if (--x == 0) {
					meta.addPage(sb.toString());
					sb = new StringBuilder();
					x = 10;
				}
				String value = container.getVariable(name).get();
				sb.append("  " + ChatColor.DARK_BLUE + name + ": " + ChatColor.BLACK + (value != null ? value : ChatColor.ITALIC + "-") + "\n");
			}
		}
		meta.addPage(sb.toString());

		if (_entityNbt instanceof MobNBT) {
			MobNBT mob = (MobNBT) _entityNbt;
			sb = new StringBuilder();
			sb.append("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Attributes:\n");
			Collection<Attribute> attributes = mob.getAttributes().values();
			if (attributes.size() == 0) {
				sb.append("  " + ChatColor.BLACK + ChatColor.ITALIC +"none\n");
			} else {
				x = 11;
				for (Attribute attribute : attributes) {
					if (x <= 3) {
						meta.addPage(sb.toString());
						sb = new StringBuilder();
						x = 11;
					}
					sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + attribute.getType().getName() + ":\n");
					sb.append("  " + ChatColor.DARK_BLUE + "Base: " + ChatColor.BLACK + attribute.getBase() + "\n");
					sb.append("  " + ChatColor.DARK_BLUE + "Modifiers:\n");
					x -= 3;
					List<Modifier> modifiers = attribute.getModifiers();
					if (modifiers.size() == 0) {
						sb.append("    " + ChatColor.BLACK + ChatColor.ITALIC +"none\n");
					} else {
						for (Modifier modifier : modifiers) {
							if (x <= 3) {
								meta.addPage(sb.toString());
								sb = new StringBuilder();
								x = 11;
							}
							sb.append("    " + ChatColor.RED + modifier.getName() + ChatColor.DARK_GREEN + " Op: " + ChatColor.BLACK + modifier.getOperation() + "\n");
							sb.append("      " + ChatColor.DARK_GREEN + "Amount: " + ChatColor.BLACK + modifier.getAmount() + "\n");
							x -= 3;
						}
					}
					sb.append("\n");
					--x;
				}
			}
			meta.addPage(sb.toString());
		}
		*/

		// LegacyBookSerialize.saveToBook(meta, _entityNbt.serialize(), _dataTitle);
		meta.addPage("RandomId: " + Integer.toHexString((new Random()).nextInt()) + "\n\n\n"
				+ ChatColor.DARK_BLUE + ChatColor.BOLD + "      The END.");
		_book.setItemMeta(meta);
		final var mojangson = _entityNbt.toString();
		NBT.modify(_book, nbt -> {
			nbt.setByteArray("BOS", mojangson.getBytes(StandardCharsets.UTF_8));
		});
	}

}