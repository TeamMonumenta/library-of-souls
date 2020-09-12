package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;

public class Region {
	private int mRegionNumber;
	private String mRegionArea;
	private String mPoiName;
	private Advancement mAdvancement;

	public Region(String poiName, int regionNumber, String regionArea, Advancement advancement) {
		mPoiName = poiName;
		mRegionNumber = regionNumber;
		mRegionArea = regionArea;
		mAdvancement = advancement;
	}

	public int getRegionNumber() {
		return this.mRegionNumber;
	}

	public String getArea() {
		return this.mRegionArea;
	}

	public String getPoiName() {
		return this.mPoiName;
	}

	public Advancement getAdvancement() {
		return this.mAdvancement;
	}

	public int getAreaNumber() {
		switch (mRegionArea) {
		case "swampnorth":
			return 1;
		case "swampsouth":
			return 2;
		case "swampeast":
			return 3;
		case "sea":
			return 4;
		case "junglenortheast":
			return 5;
		case "junglesoutheast":
			return 6;
		case "junglesouthwest":
			return 7;
		case "junglenorthwest":
			return 8;
		case "junglecenter":
			return 9;
		case "junglemountains":
			return 10;
		case "highlands":
			return 11;
		case "azacor_grove":
			return 12;
		case "chillwindforest":
			return 20;
		case "chillwindmountains":
			return 21;
		case "chillwindpermafrost":
			return 22;
		case "chillwindcrystalshroom_valley":
			return 23;
		case "chillwindtundra":
			return 24;
		case "oceanmistport":
			return 25;
		case "oceanearly":
			return 26;
		case "oceanmid-late":
			return 27;
		case "deserttrade_route":
			return 28;
		case "desertmesa":
			return 29;
		case "desertoasis":
			return 30;
		case "desertcanyonlands":
			return 31;
		default:
			return 0;
		}
	}

	public ItemStack getRegionItem() {
		switch (mRegionArea) {
		case "swampnorth":
			return new ItemStack(Material.VINE);
		case "swampsouth":
			return new ItemStack(Material.BROWN_MUSHROOM);
		case "swampeast":
			return new ItemStack(Material.PODZOL);
		case "sea":
			return new ItemStack(Material.TUBE_CORAL_BLOCK);
		case "junglenortheast":
			return new ItemStack(Material.JUNGLE_WOOD);
		case "junglesoutheast":
			return new ItemStack(Material.JUNGLE_LEAVES);
		case "junglesouthwest":
			return new ItemStack(Material.COCOA_BEANS);
		case "junglenorthwest":
			return new ItemStack(Material.JUNGLE_SAPLING);
		case "junglecenter":
			return new ItemStack(Material.ORANGE_TERRACOTTA);
		case "junglemountains":
			return new ItemStack(Material.SPRUCE_WOOD);
		case "highlands":
			return new ItemStack(Material.SPRUCE_LEAVES);
		case "azacor_grove":
			return new ItemStack(Material.FIRE_CORAL_BLOCK);
		case "chillwindforest":
			return new ItemStack(Material.SPRUCE_LOG);
		case "chillwindmountains":
			return new ItemStack(Material.GRAY_TERRACOTTA);
		case "chillwindpermafrost":
			return new ItemStack(Material.SNOW_BLOCK);
		case "chillwindcrystalshroom_valley":
			return new ItemStack(Material.MUSHROOM_STEM);
		case "chillwindtundra":
			return new ItemStack(Material.ICE);
		case "oceanmistport":
			return new ItemStack(Material.BRICK);
		case "oceanearly":
			return new ItemStack(Material.PRISMARINE);
		case "oceanmid-late":
			return new ItemStack(Material.SPONGE);
		case "deserttrade_route":
			return new ItemStack(Material.END_STONE);
		case "desertmesa":
			return new ItemStack(Material.CHISELED_RED_SANDSTONE);
		case "desertoasis":
			return new ItemStack(Material.ACACIA_LOG);
		case "desertcanyonlands":
			return new ItemStack(Material.MAGMA_BLOCK);
		default:
			return new ItemStack(Material.GRASS_BLOCK);
		}
	}

	@Override
	public String toString() {
		return mPoiName + " " + mRegionArea + " " + mRegionNumber;
	}
}
