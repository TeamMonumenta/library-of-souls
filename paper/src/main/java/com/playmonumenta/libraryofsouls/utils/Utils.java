package com.playmonumenta.libraryofsouls.utils;

import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public class Utils {
	public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
	public static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

	public static String plainText(Component formattedText) {
		// This is only legacy text because we have a bunch of section symbols lying around that need to be updated.
		String legacyText = PLAIN_SERIALIZER.serialize(formattedText);
		return plainFromLegacy(legacyText);
	}

	public static String plainFromLegacy(String legacyText) {
		return PLAIN_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(legacyText));
	}

	public static TextColor colorFromInt(int val) {
		return switch (val % 13) {
			case 0 -> NamedTextColor.DARK_GREEN;
			case 1 -> NamedTextColor.DARK_AQUA;
			case 2 -> NamedTextColor.DARK_RED;
			case 3 -> NamedTextColor.DARK_PURPLE;
			case 4 -> NamedTextColor.GOLD;
			case 5 -> NamedTextColor.GRAY;
			case 6 -> NamedTextColor.DARK_GRAY;
			case 7 -> NamedTextColor.BLUE;
			case 8 -> NamedTextColor.GREEN;
			case 9 -> NamedTextColor.AQUA;
			case 10 -> NamedTextColor.RED;
			case 11 -> NamedTextColor.LIGHT_PURPLE;
			default -> NamedTextColor.YELLOW;
		};
	}

	@SuppressWarnings("deprecation")
	public static String hashColor(String in) {
		return switch (in.hashCode() % 13) {
			case 0 -> ChatColor.DARK_GREEN + in;
			case 1 -> ChatColor.DARK_AQUA + in;
			case 2 -> ChatColor.DARK_RED + in;
			case 3 -> ChatColor.DARK_PURPLE + in;
			case 4 -> ChatColor.GOLD + in;
			case 5 -> ChatColor.GRAY + in;
			case 6 -> ChatColor.DARK_GRAY + in;
			case 7 -> ChatColor.BLUE + in;
			case 8 -> ChatColor.GREEN + in;
			case 9 -> ChatColor.AQUA + in;
			case 10 -> ChatColor.RED + in;
			case 11 -> ChatColor.LIGHT_PURPLE + in;
			default -> ChatColor.YELLOW + in;
		};
	}

	public static String getLabelFromName(Component name) throws Exception {
		String label;
		try {
			label = plainText(name).replaceAll("[^A-Za-z]", "");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to parse Library of Souls mob name '" + name + "'");
		}
		return label;
	}

	public static String getLabelFromName(String name) throws Exception {
		String label;
		try {
			label = plainFromLegacy(name).replaceAll("[^A-Za-z]", "");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to parse Library of Souls mob name '" + name + "'");
		}
		return label;
	}

	public static Component parseMiniMessage(String text) {
		return MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, false);
	}

	public static boolean insideBlocks(Location feetLoc, double width, double height) {
		BoundingBox bb = new BoundingBox(feetLoc.getX() - width/2.0,
		                                 feetLoc.getY(),
		                                 feetLoc.getZ() - width/2.0,
		                                 feetLoc.getX() + width/2.0,
		                                 feetLoc.getY() + height,
		                                 feetLoc.getZ() + width/2.0);

		int minX = (int) Math.floor(bb.getMinX());
		int minY = (int) Math.floor(bb.getMinY());
		int minZ = (int) Math.floor(bb.getMinZ());
		int maxX = (int) Math.ceil(bb.getMaxX());
		int maxY = (int) Math.ceil(bb.getMaxY());
		int maxZ = (int) Math.ceil(bb.getMaxZ());

		World world = feetLoc.getWorld();
		for (int z = minZ; z <= maxZ; z++) {
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					if (world.getBlockAt(x, y, z).getBoundingBox().overlaps(bb)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static void visitComponentDfs(Style parent, TextComponent component, BiConsumer<Style, String> consumer) {
		final var newParent = parent.merge(component.style());
		consumer.accept(newParent, component.content());

		for (Component child : component.children()) {
			if (!(child instanceof TextComponent textComponent)) {
				throw new IllegalArgumentException("Non-text component found in wrapComponent");
			}

			visitComponentDfs(newParent, textComponent, consumer);
		}
	}

	public static List<Component> wrapComponent(TextComponent text, int maxChars, int minChars, boolean skipSpace) {
		final var styleList = new ArrayList<Style>();
		final var string = new StringBuilder();

		// TODO: this isn't very good for memory...
		visitComponentDfs(Style.empty(), text, (style, s) -> {
			styleList.addAll(Collections.nCopies(s.length(), style));
			string.append(s);
		});

		List<Style> styleListCopy = styleList;

		final var components = new ArrayList<Pair<List<Style>, String>>();

		int stringBuilderIndex = 0;

		outerLoop: while (stringBuilderIndex < string.length()) {
			if (skipSpace) {
				var skipAmount = 0;
				// skip starting space characters
				while (Character.isSpaceChar(string.charAt(stringBuilderIndex))) {
					skipAmount++;
					stringBuilderIndex++;

					// if the string builder ends up empty after this operation, we can simply stop and
					// return from this function after processing the components array
					if (stringBuilderIndex >= string.length()) {
						break outerLoop;
					}
				}

				styleListCopy = styleListCopy.subList(skipAmount, styleListCopy.size());
			}

			// attempt to find a good enough candidate

			final var actualMax = Math.min(maxChars, string.length() - stringBuilderIndex);

			int splitPos = -1;
			for (int i = minChars; i < actualMax - 1; i++) {
				// look for the end of a word
				if (Character.isJavaIdentifierPart(string.charAt(stringBuilderIndex + i)) &&
					!Character.isJavaIdentifierPart(string.charAt(stringBuilderIndex + i + 1))) {
					splitPos = i + 1;
				}
			}

			// normalize split pos
			splitPos = splitPos == -1 ? actualMax : splitPos;
			final var style = styleListCopy.subList(0, splitPos);
			final var str = string.substring(stringBuilderIndex, stringBuilderIndex + splitPos);
			styleListCopy = styleListCopy.subList(splitPos, styleListCopy.size());
			stringBuilderIndex += splitPos;
			components.add(Pair.of(style, str));
		}

		// generate a compact representation of a component
		return components.stream().map(entry -> {
			if (entry.second().isEmpty()) {
				return null;
			}

			final var builder = new StringBuilder();
			var style = entry.first().get(0);

			 var component = Component.text();

			for (int i = 0; i < entry.first().size(); i++) {
				if (entry.first().get(i) != style) {
					component.append(Component.text(builder.toString(), style));
					style = entry.first().get(i);
					builder.setLength(0);
				}

				builder.append(entry.second().charAt(i));
			}

			component.append(Component.text(builder.toString(), style));
			return component.asComponent();
		}).filter(Objects::nonNull).toList();
	}
}
