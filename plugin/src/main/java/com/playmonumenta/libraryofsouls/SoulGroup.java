package com.playmonumenta.libraryofsouls;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public interface SoulGroup {
	/* This is the label-ified name, with colors and spaces stripped */
	String getLabel();

	long getModifiedOn();

	String getModifiedBy();

	Set<Soul> getPossibleSouls();

	Set<String> getPossibleSoulGroupLabels();

	Map<SoulGroup, Integer> getRandomEntries(Random random);

	Map<SoulGroup, Double> getAverageEntries();

	Map<Soul, Integer> getRandomSouls(Random random);

	Map<Soul, Double> getAverageSouls();

	/* Returns the minimum width/height of the group where they are set, otherwise null */
	Double getWidth();

	Double getHeight();

	/* Attempt to summon the soul group; may summon fewer mobs if spawn conditions are not met */
	List<Entity> summonGroup(Random random, World world, BoundingBox spawnBb);
}
