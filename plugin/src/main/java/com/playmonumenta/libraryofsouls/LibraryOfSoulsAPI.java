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

	public Set<String> getSoulNames() throws IllegalStateException {
		SoulsDatabase db = SoulsDatabase.getInstance();
		if (db == null) {
			return null;
		}
		return db.listMobNames();
	}

	public Set<String> getSoulLocations() {
		SoulsDatabase db = SoulsDatabase.getInstance();
		if (db == null) {
			return null;
		}
		return db.listMobLocations();
	}
}
