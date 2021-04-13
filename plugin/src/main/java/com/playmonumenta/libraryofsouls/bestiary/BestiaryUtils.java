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
		Integer value = availableMobs.get(soul);
		if (value != null) {
			if (value >= 2 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
				return 3;
			} else if (value >= 1 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
				return 2;
			} else if (value >= 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
				return 2;
			} else if (value >= 3 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
				return 1;
			} else if (value >= 10) {
				return 3;
			} else if (value >= 5) {
				return 2;
			} else if (value >= 1) {
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
