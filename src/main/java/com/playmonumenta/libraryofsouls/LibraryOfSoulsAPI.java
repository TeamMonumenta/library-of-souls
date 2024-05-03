package com.playmonumenta.libraryofsouls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class LibraryOfSoulsAPI {
	public static final String SOUL_PARTY_PREFIX = "#";
	public static final String SOUL_POOL_PREFIX = "~";

	public static @Nullable Entity summon(Location loc, String soulName) {
		SoulsDatabase db = SoulsDatabase.getInstance();
		SoulEntry soul = db.getSoul(soulName);
		if (soul == null) {
			return null;
		}
		return soul.summon(loc);
	}

	public static Set<String> getSoulNames() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		return db.listMobNames();
	}


	public static Set<String> getSoulPartyNames() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		return db.listSoulPartyNames();
	}

	public static Set<String> getSoulPoolNames() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		return db.listSoulPoolNames();
	}

	public static Set<String> getSoulGroupNames() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		return db.listSoulGroupNames();
	}

	public static Map<Soul, Integer> getRandomSouls(String label, Random random) {
		SoulsDatabase db = SoulsDatabase.getInstance();
		SoulGroup group = db.getSoulGroup(label);
		if (group == null) {
			return new HashMap<>();
		}
		return group.getRandomSouls(random);
	}

	public static Set<String> getSoulLocations() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		return db.listMobLocations();
	}

	public static List<Component> getDescription(String soulName) {
		SoulsDatabase db = SoulsDatabase.getInstance();
		SoulEntry soul = db.getSoul(soulName);
		if (soul == null) {
			return null;
		}
		return soul.getDescription();
	}
}
