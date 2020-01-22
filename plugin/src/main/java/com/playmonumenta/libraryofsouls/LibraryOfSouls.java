package com.playmonumenta.libraryofsouls;

import org.bukkit.plugin.java.JavaPlugin;

import com.goncalomb.bukkit.mylib.command.MyCommandManager;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;

public class LibraryOfSouls extends JavaPlugin {
	private static LibraryOfSouls INSTANCE = null;

	@Override
	public void onEnable() {
		INSTANCE = this;

		try {
			new SoulsDatabase(this);

			MyCommandManager.register(new LibraryOfSoulsCommand(), this);
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
