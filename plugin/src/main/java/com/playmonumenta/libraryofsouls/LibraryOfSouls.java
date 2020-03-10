package com.playmonumenta.libraryofsouls;

import org.bukkit.plugin.java.JavaPlugin;

import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

public class LibraryOfSouls extends JavaPlugin {
	private static LibraryOfSouls INSTANCE = null;

	@Override
	public void onLoad() {
		/*
		 * CommandAPI commands which register directly and are usable in functions
		 *
		 * These need to register immediately on load to prevent function loading errors
		 */
		LibraryOfSoulsCommand.register();
	}

	@Override
	public void onEnable() {
		INSTANCE = this;

		try {
			new SoulsDatabase(this);
		} catch (Exception e) {
			getLogger().severe("Failed to load souls database! This plugin will not function");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		INSTANCE = null;
	}

	public static LibraryOfSouls getInstance() {
		return INSTANCE;
	}
}
