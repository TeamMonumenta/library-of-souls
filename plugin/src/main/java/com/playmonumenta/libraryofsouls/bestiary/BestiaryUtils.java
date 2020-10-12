package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;

import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

public class BestiaryUtils {
	//This class should just be called Bestiary maps
	public static EnumMap<Material, Double> mDefaultArmor = new EnumMap<>(Material.class);
	public static EnumMap<EntityType, Double> mDefaultDamage = new EnumMap<>(EntityType.class);
	public static EnumMap<EntityType, Double> mDefaultSpeed = new EnumMap<>(EntityType.class);
	public static EnumMap<Material, Double> mDefaultItemDamage = new EnumMap<>(Material.class);
	public static List<String> mDungeonMap = new ArrayList<>();
	public static List<Region> mPoiMap = new ArrayList<>();
	public static List<String> mDungeons;
	public static Set<String> mLocs;
	public static Map<String, String> mLocsCompressed = new HashMap<>();

	private static final String NSWAMP = "Northern Swamplands";
	private static final String SSWAMP = "Southern Swamplands";
	private static final String ESWAMP = "Eastern Swamplands";
	private static final String CPLATEAU = "Central Plateau";
	private static final String NWJUNGLE = "Northwestern Jungle";
	private static final String SEJUNGLE = "Southeastern Jungle";
	private static final String NMOUNT = "Northern Mountains";
	private static final String NEJUNGLE = "Northeastern Jungle";
	private static final String SWJUNGLE = "Southwestern Jungle";
	private static final String GROVE = "Azacor's Grove";
	private static final String MIST = "Mistport Outlands";
	private static final String VIRIDIAN = "Viridian Archipelago";
	private static final String OCEAN = "Ocean's Reach";
	private static final String FOREST = "Forest";
	private static final String MOUNT = "Mountains";
	private static final String PERMA = "Permafrost";
	private static final String SHROOM = "Crystalshroom Valley";
	private static final String ICELANDS = "Icelands";
	private static final String TRADE = "Trade Route";
	private static final String MESA = "Mesa";
	private static final String OASIS = "Oasis";
	private static final String CANYON = "Canyonlands";

	static {
		mDefaultArmor.put(Material.LEATHER_HELMET, 1.0);
		mDefaultArmor.put(Material.LEATHER_CHESTPLATE, 3.0);
		mDefaultArmor.put(Material.LEATHER_LEGGINGS, 2.0);
		mDefaultArmor.put(Material.LEATHER_BOOTS, 1.0);
		mDefaultArmor.put(Material.GOLDEN_HELMET, 2.0);
		mDefaultArmor.put(Material.GOLDEN_CHESTPLATE, 5.0);
		mDefaultArmor.put(Material.GOLDEN_LEGGINGS, 3.0);
		mDefaultArmor.put(Material.GOLDEN_BOOTS, 1.0);
		mDefaultArmor.put(Material.CHAINMAIL_HELMET, 2.0);
		mDefaultArmor.put(Material.CHAINMAIL_CHESTPLATE, 5.0);
		mDefaultArmor.put(Material.CHAINMAIL_LEGGINGS, 4.0);
		mDefaultArmor.put(Material.CHAINMAIL_BOOTS, 1.0);
		mDefaultArmor.put(Material.IRON_HELMET, 2.0);
		mDefaultArmor.put(Material.IRON_CHESTPLATE, 6.0);
		mDefaultArmor.put(Material.IRON_LEGGINGS, 5.0);
		mDefaultArmor.put(Material.IRON_BOOTS, 2.0);
		mDefaultArmor.put(Material.DIAMOND_HELMET, 3.0);
		mDefaultArmor.put(Material.DIAMOND_CHESTPLATE, 8.0);
		mDefaultArmor.put(Material.DIAMOND_LEGGINGS, 6.0);
		mDefaultArmor.put(Material.DIAMOND_BOOTS, 3.0);
		mDefaultArmor.put(Material.TURTLE_HELMET, 2.0);

		mDefaultDamage.put(EntityType.BLAZE, 6.0);
		mDefaultDamage.put(EntityType.CAVE_SPIDER, 2.0);
		mDefaultDamage.put(EntityType.CREEPER, 49.0);
		mDefaultDamage.put(EntityType.DOLPHIN, 3.0);
		//??
		mDefaultDamage.put(EntityType.DROWNED, 3.0);
		mDefaultDamage.put(EntityType.ELDER_GUARDIAN, 8.0);
		mDefaultDamage.put(EntityType.ENDERMAN, 7.0);
		mDefaultDamage.put(EntityType.ENDERMITE, 2.0);
		mDefaultDamage.put(EntityType.EVOKER, 6.0);
		mDefaultDamage.put(EntityType.GHAST, 23.0);
		mDefaultDamage.put(EntityType.GUARDIAN, 6.0);
		mDefaultDamage.put(EntityType.HUSK, 3.0);
		//??
		mDefaultDamage.put(EntityType.ILLUSIONER, 4.0);
		mDefaultDamage.put(EntityType.IRON_GOLEM, 21.0);
		mDefaultDamage.put(EntityType.MAGMA_CUBE, 6.0);
		mDefaultDamage.put(EntityType.PHANTOM, 6.0);
		mDefaultDamage.put(EntityType.PIG_ZOMBIE, 4.0);
		//??
		mDefaultDamage.put(EntityType.PILLAGER, 4.0);
		mDefaultDamage.put(EntityType.POLAR_BEAR, 6.0);
		mDefaultDamage.put(EntityType.RAVAGER, 12.0);
		mDefaultDamage.put(EntityType.SHULKER, 4.0);
		mDefaultDamage.put(EntityType.SILVERFISH, 1.0);
		//??
		mDefaultDamage.put(EntityType.SKELETON, 2.0);
		mDefaultDamage.put(EntityType.SLIME, 2.0);
		mDefaultDamage.put(EntityType.SPIDER, 2.0);
		//??
		mDefaultDamage.put(EntityType.STRAY, 2.0);
		mDefaultDamage.put(EntityType.VEX, 9.0);
		mDefaultDamage.put(EntityType.VINDICATOR, 13.0);
		mDefaultDamage.put(EntityType.WITHER_SKELETON, 8.0);
		//??
		mDefaultDamage.put(EntityType.WITHER, 8.0);
		mDefaultDamage.put(EntityType.WOLF, 2.0);
		mDefaultDamage.put(EntityType.ZOMBIE, 3.0);
		mDefaultDamage.put(EntityType.ZOMBIE_VILLAGER, 3.0);
		//I'll just assume it works the same for each mob-it should really only be on select zombies anyway
		mDefaultItemDamage.put(Material.WOODEN_SWORD, 4.0);
		mDefaultItemDamage.put(Material.GOLDEN_SWORD, 4.0);
		mDefaultItemDamage.put(Material.STONE_SWORD, 5.0);
		mDefaultItemDamage.put(Material.IRON_SWORD, 6.0);
		mDefaultItemDamage.put(Material.DIAMOND_SWORD, 7.0);

		mDefaultSpeed.put(EntityType.SNOWMAN, 0.2);
		mDefaultSpeed.put(EntityType.BLAZE, 0.23);
		mDefaultSpeed.put(EntityType.DROWNED, 0.23);
		mDefaultSpeed.put(EntityType.HUSK, 0.23);
		mDefaultSpeed.put(EntityType.ZOMBIE, 0.23);
		mDefaultSpeed.put(EntityType.ZOMBIE_VILLAGER, 0.23);
		mDefaultSpeed.put(EntityType.PIG_ZOMBIE, 0.23);
		mDefaultSpeed.put(EntityType.CREEPER, 0.25);
		mDefaultSpeed.put(EntityType.ENDERMITE, 0.25);
		mDefaultSpeed.put(EntityType.IRON_GOLEM, 0.25);
		mDefaultSpeed.put(EntityType.POLAR_BEAR, 0.25);
		mDefaultSpeed.put(EntityType.SILVERFISH, 0.25);
		mDefaultSpeed.put(EntityType.SKELETON, 0.25);
		mDefaultSpeed.put(EntityType.STRAY, 0.25);
		mDefaultSpeed.put(EntityType.WITCH, 0.25);
		mDefaultSpeed.put(EntityType.WITHER_SKELETON, 0.25);
		mDefaultSpeed.put(EntityType.CAT, 0.3);
		mDefaultSpeed.put(EntityType.CAVE_SPIDER, 0.3);
		mDefaultSpeed.put(EntityType.ELDER_GUARDIAN, 0.3);
		mDefaultSpeed.put(EntityType.ENDERMAN, 0.3);
		mDefaultSpeed.put(EntityType.FOX, 0.3);
		mDefaultSpeed.put(EntityType.OCELOT, 0.3);
		mDefaultSpeed.put(EntityType.RAVAGER, 0.3);
		mDefaultSpeed.put(EntityType.SPIDER, 0.3);
		mDefaultSpeed.put(EntityType.WOLF, 0.3);
		mDefaultSpeed.put(EntityType.PILLAGER, 0.35);
		mDefaultSpeed.put(EntityType.VINDICATOR, 0.35);
		mDefaultSpeed.put(EntityType.EVOKER, 0.5);
		mDefaultSpeed.put(EntityType.GUARDIAN, 0.5);
		mDefaultSpeed.put(EntityType.ILLUSIONER, 0.5);
		mDefaultSpeed.put(EntityType.WITHER, 0.6);
		mDefaultSpeed.put(EntityType.GHAST, 0.7);
		mDefaultSpeed.put(EntityType.PUFFERFISH, 0.7);
		mDefaultSpeed.put(EntityType.SHULKER, 0.0);
		mDefaultSpeed.put(EntityType.DOLPHIN, 1.2);
		mDefaultSpeed.put(EntityType.SQUID, 0.7);
		mDefaultSpeed.put(EntityType.VEX, 0.7);

		mDungeonMap.add("white");
		mDungeonMap.add("orange");
		mDungeonMap.add("magenta");
		mDungeonMap.add("lightblue");
		mDungeonMap.add("yellow");
		mDungeonMap.add("lime");
		mDungeonMap.add("pink");
		mDungeonMap.add("gray");
		mDungeonMap.add("lightgray");
		mDungeonMap.add("cyan");
		mDungeonMap.add("purple");
		mDungeonMap.add("labs");
		mDungeonMap.add("willows");
		mDungeonMap.add("sanctum");
		mDungeonMap.add("roguelike");
		mDungeonMap.add("reverie");
		mDungeonMap.add("shiftingcity");
		mDungeonMap.add("region_1");
		mDungeonMap.add("region_2");
	}

	public static String formatWell(String in) {
		if (in.equals("lightblue")) {
			return "Light Blue";
		} else if (in.equals("lightgray")) {
			return "Light Gray";
		} else if (in.equals("maw")) {
			return "The Grand Maw";
		} else if (in.equals("monastery")) {
			return "Axtan Monastery";
		} else if (in.equals("tree")) {
			return "Hawk Fortress";
		} else if (in.equals("tree_winter")) {
			return "Winter Fortress";
		} else if (in.equals("eastern_bandit_camp")) {
			return "Bandit Stronghold";
		} else if (in.equals("bandit_fortress")) {
			return "Bandit Camp";
		} else if (in.equals("cliffside_bandits")) {
			return "Mercenary Fort";
		} else if (in.equals("northwestern_mine")) {
			return "Northern Mineshaft";
		} else if (in.equals("eastern_mine")) {
			return "Eastern Mineshaft";
		} else if (in.equals("southeastern_mine")) {
			return "Southeastern Mineshaft";
		} else if (in.equals("southern_mine")) {
			return "Southern Mineshaft";
		} else if (in.equals("region_1")) {
			return "Region 1";
		} else if (in.equals("region_2")) {
			return "Region 2";
		} else if (in.equals("shiftingcity")) {
			return "Shifting City";
		}
		in = in.replaceAll("\"", "");
		String sub = "";
		if (in.contains("_")) {
			String[] cuts = in.split("_");
			for (String cut : cuts) {
				if (cut.length() == 0) {
					continue;
				}
				String subCut = cut.substring(0, 1);
				subCut = subCut.toUpperCase();
				subCut += cut.substring(1);
				sub += subCut + " ";
			}
		} else {
			sub = in.substring(0, 1);
			sub = sub.toUpperCase();
			sub += in.substring(1);
		}
		return sub;
	}

	public static void registerPoiLocs() {
		mLocs = SoulsDatabase.getInstance().listMobLocations();
		for (String loc : mLocs) {
			mLocsCompressed.put(removeUnderscore(loc), loc);
		}

		Iterator<Advancement> advancements = Bukkit.advancementIterator();
		while (advancements.hasNext()) {
			Advancement adv = advancements.next();
			String advancement = adv.getKey().getKey();
			if (!isPoiAdvancement(advancement)) {
				continue;
			}

			String[] roots = advancement.split("/");

			if (roots[1].equals("r1")) {
				String area = roots[2];
				String loc = roots[3];
				if (roots.length == 5) {
					area = roots[2] + roots[3];
					loc = roots[4];
				}

				if (mLocsCompressed.containsKey(loc)) {
					loc = mLocsCompressed.get(loc);
				} else {
					Bukkit.broadcastMessage(advancement);
					continue;
				}

				mPoiMap.add(new Region(loc, 1, area, adv));
			} else if (roots[1].equals("r2")) {
				String area = roots[2] + roots[3];
				String loc = roots[4];
				if (!mLocs.contains(loc)) {
					Bukkit.broadcastMessage(advancement);
					continue;
				}
				mPoiMap.add(new Region(loc, 2, area, adv));
			}
		}
	}

	public static void registerDungeons() {
		Iterator<Advancement> advancements = Bukkit.advancementIterator();

		while (advancements.hasNext()) {
			Advancement adv = advancements.next();
		}
	}

	public static String hashColor(String in) {
		switch(in) {
		case "white":
			return ChatColor.WHITE + "";
		case "orange":
			return ChatColor.GOLD + "";
		case "magenta":
			return ChatColor.LIGHT_PURPLE + "";
		case "lightblue":
			return ChatColor.BLUE + "";
		case "yellow":
			return ChatColor.YELLOW + "";
		case "lime":
			return ChatColor.GREEN + "";
		case "pink":
			return ChatColor.RED + "";
		case "gray":
			return ChatColor.DARK_GRAY + "";
		case "lightgray":
			return ChatColor.GRAY + "";
		case "cyan":
			return ChatColor.AQUA + "";
		case "purple":
			return ChatColor.DARK_PURPLE + "";
		case "blue":
			return ChatColor.BLUE + "";
		case "brown":
			return ChatColor.DARK_AQUA + "";
		case "green":
			return ChatColor.GREEN + "";
		case "red":
			return ChatColor.DARK_RED + "";
		case "black":
			return ChatColor.BLACK + "";
		case "shiftingcity":
			return ChatColor.AQUA + "";
		case "willows":
			return ChatColor.DARK_GREEN + "";
		case "reverie":
			return ChatColor.DARK_RED + "";
		case "roguelike":
			return ChatColor.DARK_RED + "";
		case "sanctum":
			return ChatColor.GREEN + "";
		default:
			return ChatColor.BLACK + "";
		}
	}

	public static int getInfoTier(Soul soul, Map<SoulEntry, Integer> availableMobs) {
		if (availableMobs.containsKey(soul)) {
			if (availableMobs.get(soul) >= 2 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
				return 3;
			} else if (availableMobs.get(soul) >= 1 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
				return 2;
			} else if (availableMobs.get(soul) >= 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
				return 2;
			} else if (availableMobs.get(soul) >= 3 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
				return 1;
			} else if (availableMobs.get(soul) >= 10) {
				return 3;
			} else if (availableMobs.get(soul) >= 5) {
				return 2;
			} else if (availableMobs.get(soul) >= 1) {
				return 1;
			}
		}
		return 0;
	}

	public static String nameToHex(String name) {
		return Integer.toHexString(name.hashCode());
	}

	private static boolean isPoiAdvancement(String in) {
		return in.startsWith("pois") && !in.endsWith("root") && !in.endsWith("trigger");
	}

	private static String removeUnderscore(String in) {
		return in.replaceAll("_", "");
	}
}
