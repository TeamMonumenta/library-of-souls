package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Region {
	private int mRegionNumber;
	private String mRegionArea;
	private String mPoiName;

	public Region(String poiName, int regionNumber, String regionArea) {
		mPoiName = poiName;
		mRegionNumber = regionNumber;
		mRegionArea = regionArea;
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

	public int getAreaNumber() {
		switch (mRegionArea) {
		case "Northern Swamplands":
			return 1;
		case "Southern Swamplands":
			return 2;
		case "Eastern Swamplands":
			return 3;
		case "Northeastern Jungle":
			return 4;
		case "Southeastern Jungle":
			return 5;
		case "Southwestern Jungle":
			return 6;
		case "Northwestern Jungle":
			return 7;
		case "Central Plateau":
			return 8;
		case "Northern Mountains":
			return 9;
		case "Highlands":
			return 10;
		case "Azacor's Grove":
			return 11;
		case "Forest":
			return 20;
		case "Mountains":
			return 21;
		case "Permafrost":
			return 22;
		case "Crystalshroom Valley":
			return 23;
		case "Icelands":
			return 24;
		case "Mistport Outlands":
			return 25;
		case "Viridian Archipelago":
			return 26;
		case "Ocean's Reach":
			return 27;
		case "Trade Route":
			return 28;
		case "Mesa":
			return 29;
		case "Oasis":
			return 30;
		case "Canyonlands":
			return 31;
		default:
			return 0;
		}
	}

	public ItemStack getRegionItem() {
		switch (mRegionArea) {
		case "Northern Swamplands":
			return new ItemStack(Material.VINE);
		case "Southern Swamplands":
			return new ItemStack(Material.BROWN_MUSHROOM);
		case "Eastern Swamplands":
			return new ItemStack(Material.PODZOL);
		case "Northeastern Jungle":
			return new ItemStack(Material.JUNGLE_WOOD);
		case "Southeastern Jungle":
			return new ItemStack(Material.JUNGLE_LEAVES);
		case "Southwestern Jungle":
			return new ItemStack(Material.COCOA_BEANS);
		case "Northwestern Jungle":
			return new ItemStack(Material.JUNGLE_SAPLING);
		case "Central Plateau":
			return new ItemStack(Material.ORANGE_TERRACOTTA);
		case "Northern Mountains":
			return new ItemStack(Material.SPRUCE_WOOD);
		case "Highlands":
			return new ItemStack(Material.SPRUCE_LEAVES);
		case "Azacor's Grove":
			return new ItemStack(Material.FIRE_CORAL_BLOCK);
		case "Forest":
			return new ItemStack(Material.SPRUCE_LOG);
		case "Mountains":
			return new ItemStack(Material.GRAY_TERRACOTTA);
		case "Permafrost":
			return new ItemStack(Material.SNOW_BLOCK);
		case "Crystalshroom Valley":
			return new ItemStack(Material.MUSHROOM_STEM);
		case "Icelands":
			return new ItemStack(Material.ICE);
		case "Mistport Outlands":
			return new ItemStack(Material.BRICK);
		case "Viridian Archipelago":
			return new ItemStack(Material.PRISMARINE);
		case "Ocean's Reach":
			return new ItemStack(Material.SPONGE);
		case "Trade Route":
			return new ItemStack(Material.END_STONE);
		case "Mesa":
			return new ItemStack(Material.CHISELED_RED_SANDSTONE);
		case "Oasis":
			return new ItemStack(Material.ACACIA_LOG);
		case "Canyonlands":
			return new ItemStack(Material.MAGMA_BLOCK);
		default:
			return new ItemStack(Material.GRASS);
		}
	}
}
