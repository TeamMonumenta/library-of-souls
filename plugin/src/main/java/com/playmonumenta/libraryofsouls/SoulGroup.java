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

	Map<Soul, Integer> getRandomEntries(Random random);

	Map<Soul, Double> getAverageEntries();
}
