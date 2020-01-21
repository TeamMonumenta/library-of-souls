package com.playmonumenta.libraryofsouls;

import org.bukkit.plugin.java.JavaPlugin;

import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

public class LibraryOfSouls extends JavaPlugin {
	private static LibraryOfSouls INSTANCE = null;

	@Override
	public void onEnable() {
		INSTANCE = this;

		getCommand("los").setExecutor(new LibraryOfSoulsCommand());
	}

	@Override
	public void onDisable() {
		INSTANCE = null;
	}

	public static LibraryOfSouls getInstance() {
		return INSTANCE;
	}
}
