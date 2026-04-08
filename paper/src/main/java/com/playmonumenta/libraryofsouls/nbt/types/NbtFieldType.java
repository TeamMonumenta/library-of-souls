package com.playmonumenta.libraryofsouls.nbt.types;

import com.playmonumenta.libraryofsouls.nbt.BookOfSouls;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Describes the expected type of a single NBT field on an entity, and handles both
 * display formatting and player interaction (SNBT input or GUI) for that field.
 */
@SuppressWarnings("ClassInitializationDeadlock")
public abstract class NbtFieldType {

	// === Abstract interface ===

	public abstract Component formatForBook(ReadableNBT nbt, String key);

	public abstract void interact(Player player, BookOfSouls bos, String key)
		throws WrapperCommandSyntaxException;

	public abstract void setFromInput(Player player, BookOfSouls bos, String key, String input)
		throws WrapperCommandSyntaxException;

	public List<String> possibleValues() {
		return List.of();
	}

	public String hint() {
		return "";
	}

	// === Fallback for unannotated / unknown keys ===

	private static final NbtFieldType GENERIC_INSTANCE = new SimplePrimitive("") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return rawFormat(nbt, key);
		}
	};

	public static NbtFieldType generic() {
		return GENERIC_INSTANCE;
	}

	/** Raw formatter used by generic() and any type that delegates to it. */
	static Component rawFormat(ReadableNBT nbt, String key) {
		String raw = switch (nbt.getType(key)) {
			case NBTTagByte -> nbt.getByte(key) + "b";
			case NBTTagShort -> nbt.getShort(key) + "s";
			case NBTTagInt -> String.valueOf(nbt.getInteger(key));
			case NBTTagLong -> nbt.getLong(key) + "L";
			case NBTTagFloat -> nbt.getFloat(key) + "f";
			case NBTTagDouble -> nbt.getDouble(key) + "d";
			case NBTTagString -> "\"" + nbt.getString(key) + "\"";
			case NBTTagByteArray -> "[B;" + nbt.getByteArray(key).length + " bytes]";
			case NBTTagIntArray -> "[I;" + nbt.getIntArray(key).length + " ints]";
			case NBTTagLongArray -> "[L;" + nbt.getLongArray(key).length + " longs]";
			case NBTTagList -> "[" + nbt.getListType(key).name().replace("NBTTag", "").toLowerCase(Locale.ROOT) + " list]";
			case NBTTagCompound -> {
				ReadableNBT sub = nbt.getCompound(key);
				yield sub != null ? sub.toString() : "{}";
			}
			default -> "?";
		};
		if (raw.length() > 60) {
			raw = raw.substring(0, 59) + "\u2026";
		}
		return Component.text(raw);
	}

	// === Shared base for all non-GUI types ===

	abstract static class SimplePrimitive extends NbtFieldType {

		final String mHint;

		SimplePrimitive(String hint) {
			mHint = hint;
		}

		@Override
		public String hint() {
			return mHint;
		}

		@Override
		public void interact(Player player, BookOfSouls bos, String key) {
			ReadWriteNBT nbt = bos.getEntityNBT();
			Component hintSuffix = mHint.isEmpty()
				? Component.empty()
				: Component.text(" [" + mHint + "]", NamedTextColor.GRAY);
			if (!nbt.hasTag(key)) {
				player.sendMessage(
					Component.text(key + " is not set", NamedTextColor.RED).append(hintSuffix));
				return;
			}
			player.sendMessage(
				Component.text(key + ": ", NamedTextColor.AQUA)
					.append(formatForBook(nbt, key))
					.append(hintSuffix));
		}

		@Override
		public void setFromInput(Player player, BookOfSouls bos, String key, String input)
				throws WrapperCommandSyntaxException {
			applyInput(bos.getEntityNBT(), key, input);
			bos.saveBook();
			player.getInventory().setItemInMainHand(bos.getBook());
			player.sendMessage(Component.text("'" + key + "' set.", NamedTextColor.GREEN));
		}

		/** Default: raw SNBT parse + merge. Override for coercing numeric types. */
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			target.mergeCompound(parseSafe(key, input));
		}
	}

	// === Primitive singletons ===

	public static final NbtFieldType BOOLEAN = new SimplePrimitive("boolean") {
		@Override
		public List<String> possibleValues() {
			return List.of("true", "false");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getByte(key) != 0 ? "true" : "false");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			String lower = input.toLowerCase(Locale.ROOT).trim();
			if (lower.equals("true") || lower.equals("1") || lower.equals("1b")) {
				target.setByte(key, (byte) 1);
				return;
			}
			if (lower.equals("false") || lower.equals("0") || lower.equals("0b")) {
				target.setByte(key, (byte) 0);
				return;
			}
			try {
				String num = lower.endsWith("b") ? lower.substring(0, lower.length() - 1) : lower;
				target.setByte(key, Byte.parseByte(num));
			} catch (NumberFormatException e) {
				throw CommandAPI.failWithString("Expected boolean (true/false/0/1), got: " + input);
			}
		}
	};

	public static final NbtFieldType BYTE = new SimplePrimitive("byte") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getByte(key) + "b");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			byte value = switch (parsed.getType(key)) {
				case NBTTagByte -> parsed.getByte(key);
				case NBTTagInt -> {
					int i = parsed.getInteger(key);
					if (i < Byte.MIN_VALUE || i > Byte.MAX_VALUE) {
						throw CommandAPI.failWithString("Value " + i + " out of byte range ("
							+ Byte.MIN_VALUE + " to " + Byte.MAX_VALUE + ")");
					}
					yield (byte) i;
				}
				default -> throw CommandAPI.failWithString("Expected byte (e.g. 5b or 5), got: " + input);
			};
			target.setByte(key, value);
		}
	};

	public static final NbtFieldType SHORT = new SimplePrimitive("short") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getShort(key) + "s");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			short value = switch (parsed.getType(key)) {
				case NBTTagShort -> parsed.getShort(key);
				case NBTTagInt -> {
					int i = parsed.getInteger(key);
					if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
						throw CommandAPI.failWithString("Value " + i + " out of short range");
					}
					yield (short) i;
				}
				default -> throw CommandAPI.failWithString("Expected short (e.g. 5s or 5), got: " + input);
			};
			target.setShort(key, value);
		}
	};

	public static final NbtFieldType INT = new SimplePrimitive("int") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(String.valueOf(nbt.getInteger(key)));
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			if (parsed.getType(key) != NBTType.NBTTagInt) {
				throw CommandAPI.failWithString("Expected int (e.g. 42), got: " + input);
			}
			target.setInteger(key, parsed.getInteger(key));
		}
	};

	public static final NbtFieldType LONG = new SimplePrimitive("long") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getLong(key) + "L");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			long value = switch (parsed.getType(key)) {
				case NBTTagLong -> parsed.getLong(key);
				case NBTTagInt -> (long) parsed.getInteger(key);
				default -> throw CommandAPI.failWithString("Expected long (e.g. 5L or 5), got: " + input);
			};
			target.setLong(key, value);
		}
	};

	public static final NbtFieldType FLOAT = new SimplePrimitive("float") {
		@Override
		public List<String> possibleValues() {
			return List.of("1.0f");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getFloat(key) + "f");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			float value;
			switch (parsed.getType(key)) {
				case NBTTagFloat -> value = parsed.getFloat(key);
				case NBTTagDouble -> {
					double d = parsed.getDouble(key);
					value = (float) d;
				}
				case NBTTagInt -> value = (float) parsed.getInteger(key);
				case NBTTagLong -> {
					long l = parsed.getLong(key);
					value = (float) l;
				}
				default -> throw CommandAPI.failWithString("Expected float (e.g. 1.5f), got: " + input);
			}
			target.setFloat(key, value);
		}
	};

	public static final NbtFieldType DOUBLE = new SimplePrimitive("double") {
		@Override
		public List<String> possibleValues() {
			return List.of("1.0");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text(nbt.getDouble(key) + "d");
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			double value;
			switch (parsed.getType(key)) {
				case NBTTagDouble -> value = parsed.getDouble(key);
				case NBTTagFloat -> {
					float f = parsed.getFloat(key);
					value = (double) f;
				}
				case NBTTagInt -> value = (double) parsed.getInteger(key);
				case NBTTagLong -> {
					long l = parsed.getLong(key);
					value = (double) l;
				}
				default -> throw CommandAPI.failWithString("Expected double (e.g. 1.5 or 1.5d), got: " + input);
			}
			target.setDouble(key, value);
		}
	};

	public static final NbtFieldType STRING = new SimplePrimitive("string") {
		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			String val = nbt.getString(key);
			String display = "\"" + (val != null ? val : "") + "\"";
			if (display.length() > 60) {
				display = display.substring(0, 59) + "\u2026\"";
			}
			return Component.text(display);
		}

		@Override
		void applyInput(ReadWriteNBT target, String key, String input)
				throws WrapperCommandSyntaxException {
			ReadWriteNBT parsed = parseSafe(key, input);
			if (parsed.getType(key) != NBTType.NBTTagString) {
				throw CommandAPI.failWithString("Expected string (use \"quotes\" around value), got: " + input);
			}
			target.mergeCompound(parsed);
		}
	};

	public static final NbtFieldType COMPOUND = new SimplePrimitive("compound") {
		@Override
		public List<String> possibleValues() {
			return List.of("{}");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			ReadableNBT sub = nbt.getCompound(key);
			String s = sub != null ? sub.toString() : "{}";
			if (s.length() > 60) {
				s = s.substring(0, 59) + "\u2026";
			}
			return Component.text(s);
		}
	};

	public static final NbtFieldType LIST = new SimplePrimitive("list") {
		@Override
		public List<String> possibleValues() {
			return List.of("[]");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return rawFormat(nbt, key);
		}
	};

	public static final NbtFieldType INT_ARRAY = new SimplePrimitive("int-array") {
		@Override
		public List<String> possibleValues() {
			return List.of("[I;0,0,0,0]");
		}

		@Override
		public Component formatForBook(ReadableNBT nbt, String key) {
			return Component.text("[I;" + nbt.getIntArray(key).length + " ints]");
		}
	};

	// GUI types — package-private implementations in separate files
	public static final NbtFieldType HAND_ITEMS = new ItemsFieldType(2);
	public static final NbtFieldType ARMOR_ITEMS = new ItemsFieldType(4);
	public static final NbtFieldType ACTIVE_EFFECTS = new EffectsFieldType(9);
	public static final NbtFieldType PASSENGERS = new PassengersFieldType(5);

	// === Shared parse helper ===

	static ReadWriteNBT parseSafe(String key, String input) throws WrapperCommandSyntaxException {
		try {
			return NBT.parseNBT("{" + key + ":" + input + "}");
		} catch (Exception e) {
			throw CommandAPI.failWithString("Invalid value for '" + key + "': " + e.getMessage());
		}
	}
}
