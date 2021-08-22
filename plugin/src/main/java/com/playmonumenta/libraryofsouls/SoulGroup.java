package com.playmonumenta.libraryofsouls;

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
}
