package com.playmonumenta.libraryofsouls;

import com.playmonumenta.libraryofsouls.bestiary.BestiaryArea;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryCommand;
import com.playmonumenta.libraryofsouls.bestiary.BestiaryManager;
import com.playmonumenta.libraryofsouls.commands.LibraryOfSoulsCommand;
import com.playmonumenta.libraryofsouls.commands.SpawnerNBTCommand;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class LibraryOfSouls extends JavaPlugin {
	private static @Nullable LibraryOfSouls INSTANCE = null;

	public static class Config {
		private static boolean mReadOnly = true;
		private static @Nullable BestiaryArea mBestiary = null;

		static void load(Logger logger, File dataFolder, boolean loadBestiary) {
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

			if (loadBestiary) {
				/* Bestiary config file */
				configFile = new File(dataFolder, "bestiary_config.yml");
				if (configFile.exists() && configFile.isFile()) {
					try {
						FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);

						if (yamlConfig.isConfigurationSection("bestiary")) {
							mBestiary = new BestiaryArea(null, "Areas", yamlConfig.getConfigurationSection("bestiary"));
						}
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

		public static @Nullable BestiaryArea getBestiary() {
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
			/* Don't load the bestiary config initially - this will happen when the souls database loads */
			Config.load(getLogger(), getDataFolder(), false);

			getLogger().info("Library of Souls read only: " + Config.isReadOnly());

			if (!Config.isReadOnly()) {
				LibraryOfSoulsCommand.registerWriteAccessCommands();
				BestiaryCommand.registerWriteAccessCommands();
			}

			new SoulsDatabase(this, !Config.isReadOnly());
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
		if (INSTANCE == null) {
			throw new RuntimeException("LibraryOfSouls not loaded");
		}
		return INSTANCE;
	}
}
