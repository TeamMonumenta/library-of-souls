package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.playmonumenta.libraryofsouls.bestiary.BestiaryManager;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;
import com.playmonumenta.libraryofsouls.commands.SpawnerNBTCommand;

public class LibraryOfSouls extends JavaPlugin {
	private static LibraryOfSouls INSTANCE = null;

	public static class Config {
		private static boolean mReadOnly = true;

		static void load(Logger logger, File dataFolder) {
			File configFile = new File(dataFolder, "config.yml");

			if (configFile.exists() && configFile.isFile()) {
				FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);

				if (yamlConfig.isBoolean("read_only")) {
					mReadOnly = yamlConfig.getBoolean("read_only", mReadOnly);
				}
			} else {
				try {
					configFile.getParentFile().mkdirs();
					FileConfiguration yamlConfig = new YamlConfiguration();
					yamlConfig.set("read_only", mReadOnly);
					yamlConfig.save(configFile);
				} catch (IOException ex) {
					logger.warning("Failed to save default config to '" + configFile.getPath() + "': " + ex.getMessage());
				}
			}
		}

		public static boolean isReadOnly() {
			return mReadOnly;
		}
	}

	@Override
	public void onLoad() {
		/*
		 * CommandAPI commands which register directly and are usable in functions
		 *
		 * These need to register immediately on load to prevent function loading errors
		 */
		LibraryOfSoulsCommand.register();
		SpawnerNBTCommand.register();
	}

	@Override
	public void onEnable() {
		INSTANCE = this;

		getServer().getPluginManager().registerEvents(new BestiaryManager(this), this);

		File directory = getDataFolder();
		if (!directory.exists()) {
			directory.mkdirs();
		}

		try {
			Config.load(getLogger(), getDataFolder());

			getLogger().info("Library of Souls read only: " + Boolean.toString(Config.isReadOnly()));

			if (!Config.isReadOnly()) {
				LibraryOfSoulsCommand.registerWriteAccessCommands();
			}

			new SoulsDatabase(this);
		} catch (Exception e) {
			getLogger().severe("Failed to load souls database! This plugin will not function");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		INSTANCE = null;
		Bukkit.getScheduler().cancelTasks(this);
	}

	public static LibraryOfSouls getInstance() {
		return INSTANCE;
	}
}
