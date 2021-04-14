package com.playmonumenta.libraryofsouls;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.playmonumenta.libraryofsouls.bestiary.BestiaryArea;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryCommand;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryManager;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;
import com.playmonumenta.libraryofsouls.commands.SpawnerNBTCommand;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LibraryOfSouls extends JavaPlugin {
	private static LibraryOfSouls INSTANCE = null;

	public static class Config {
		private static boolean mReadOnly = true;
		private static BestiaryArea mBestiary = null;

		static void load(Logger logger, File dataFolder) {
			/* Main config file, currently mostly unused */
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

			/* Bestiary config file */
			configFile = new File(dataFolder, "bestiary_config.yml");
			if (configFile.exists() && configFile.isFile()) {
				FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);

				if (yamlConfig.isConfigurationSection("bestiary") && SoulsDatabase.getInstance() != null) {
					try {
						mBestiary = new BestiaryArea(null, "Areas", yamlConfig.getConfigurationSection("bestiary"));
					} catch (Exception ex) {
						logger.severe("Failed to load bestiary configuration: " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}
		}

		public static boolean isReadOnly() {
			return mReadOnly;
		}

		public static BestiaryArea getBestiary() {
			return mBestiary;
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
		BestiaryCommand.register();
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
