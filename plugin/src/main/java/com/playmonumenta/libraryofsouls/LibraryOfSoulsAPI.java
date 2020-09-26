package com.playmonumenta.libraryofsouls;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class LibraryOfSoulsAPI {
	public static Entity summon(Location loc, String soulName) {
		SoulsDatabase db = SoulsDatabase.getInstance();
		if (db == null) {
			return null;
		}
		SoulEntry soul = db.getSoul(soulName);
		if (soul == null) {
			return null;
		}
		return soul.summon(loc);
	}

	public static Set<String> getSoulNames() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		if (db == null) {
			return null;
		}
		return db.listMobNames();
	}

	public static Set<String> getSoulLocations() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		if (db == null) {
			return null;
		}
		return db.listMobLocations();
	}
}
