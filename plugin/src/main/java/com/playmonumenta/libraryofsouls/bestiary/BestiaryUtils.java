package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import com.playmonumenta.libraryofsouls.Soul;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class BestiaryUtils {
	//This class should just be called Bestiary maps
	public static EnumMap<Material, Double> mDefaultArmor = new EnumMap<>(Material.class);
	public static EnumMap<EntityType, Double> mDefaultDamage = new EnumMap<>(EntityType.class);
	public static EnumMap<EntityType, Double> mDefaultSpeed = new EnumMap<>(EntityType.class);
	public static HashMap<ItemStack, Double> mDefaultItemDamage = new HashMap<>();
	public static List<String> mBookMap = new ArrayList<>();
	public static List<Region> mPoiMap = new ArrayList<>();
	public static List<String> mDungeons;

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
		mDefaultItemDamage.put(new ItemStack(Material.WOODEN_SWORD), 4.0);
		mDefaultItemDamage.put(new ItemStack(Material.GOLDEN_SWORD), 4.0);
		mDefaultItemDamage.put(new ItemStack(Material.STONE_SWORD), 5.0);
		mDefaultItemDamage.put(new ItemStack(Material.IRON_SWORD), 6.0);
		mDefaultItemDamage.put(new ItemStack(Material.DIAMOND_SWORD), 7.0);

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

		mBookMap.add("white");
		mBookMap.add("orange");
		mBookMap.add("magenta");
		mBookMap.add("lightblue");
		mBookMap.add("yellow");
		mBookMap.add("lime");
		mBookMap.add("pink");
		mBookMap.add("gray");
		mBookMap.add("lightgray");
		mBookMap.add("cyan");
		mBookMap.add("purple");
		mBookMap.add("labs");
		mBookMap.add("willows");
		mBookMap.add("roguelike");
		mBookMap.add("reverie");
		mBookMap.add("shifting_city");
		mBookMap.add("region_1");
		mBookMap.add("region_2");

		mPoiMap.add(new Region("eastern_bandit_camp", 1, NSWAMP));
		mPoiMap.add(new Region("bandit_fortress", 1, NSWAMP));
		mPoiMap.add(new Region("cliffside_bandits", 1, NSWAMP));
		mPoiMap.add(new Region("northwestern_mine", 1, NSWAMP));
		mPoiMap.add(new Region("eastern_mine", 1, ESWAMP));
		mPoiMap.add(new Region("southeastern_mine", 1, SSWAMP));
		mPoiMap.add(new Region("water_shrine", 1, ESWAMP));
		mPoiMap.add(new Region("fire_shrine", 1, ESWAMP));
		mPoiMap.add(new Region("earth_shrine", 1, SWJUNGLE));
		mPoiMap.add(new Region("air_shrine", 1, CPLATEAU));
		mPoiMap.add(new Region("feyrune_forest", 2, MIST));
		mPoiMap.add(new Region("barracks_at_sea", 2, VIRIDIAN));
		mPoiMap.add(new Region("city_of_bones", 2, OCEAN));
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

	public static String toNiceName(Soul soul) {
//		if (!soul.getName().substring(1, 1).equals("\"")) {
//			return ChatColor.WHITE + soul.getName().substring(9, soul.getName().length() - 2);
//		} else
		if (soul.getName().substring(1, 1).equals("\"")) {
			return ChatColor.WHITE + soul.getName().substring(1, soul.getName().length() - 1);
		} else if (soul.getName().startsWith("[")) {
			return ChatColor.WHITE + soul.getName().substring(1, soul.getName().length() - 1);
		} else {
			return ChatColor.WHITE + soul.getName().substring(1, soul.getName().length() - 1);
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
		default:
			return ChatColor.BLACK + "";
		}
	}
}
