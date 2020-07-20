package com.playmonumenta.libraryofsouls.commands;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.IntegerArgument;
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;
import io.github.jorelali.commandapi.api.exceptions.WrapperCommandSyntaxException;
import net.md_5.bungee.api.ChatColor;

public class SpawnerNBTCommand {

	public static void registerType(String method) {
		CommandPermission perms = CommandPermission.fromString("monumenta.spawnerNBT");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put(method, new LiteralArgument(method));
		arguments.put("integer", new IntegerArgument());

		CommandAPI.getInstance().register("nbtheldspawner", perms, arguments, (sender, args) -> {
			changeSpawnerNBT(method, (Integer)args[0], (Player)sender);
		});
	}

	public static void register() {
		registerType("maxspawndelay");
		registerType("minspawndelay");
		registerType("activationrange");
		registerType("spawncount");
		registerType("spawnrange");
	}

	private static void changeSpawnerNBT(String method, int argument, Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (!item.getType().equals(Material.SPAWNER)) {
			return;
		}
		BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
		CreatureSpawner spawner = (CreatureSpawner)meta.getBlockState();
		List<String> lore = item.getLore();
		switch(method) {
		case "maxspawndelay":
			if (argument < spawner.getMinSpawnDelay()) {
				CommandAPI.fail("Maximum Spawn Delay cannot be smaller than Minimum Spawn Delay!");
				return;
			}
			lore.set(lore.indexOf(ChatColor.WHITE + "Max Spawn Delay: " + spawner.getMaxSpawnDelay()), ChatColor.WHITE + "Max Spawn Delay: " + argument);
			spawner.setMaxSpawnDelay(argument);
		case "minspawndelay":
			if (argument > spawner.getMaxSpawnDelay()) {
				CommandAPI.fail("Minimum Spawn Delay cannot be larger than Maximum Spawn Delay!");
				return;
			}
			lore.set(lore.indexOf(ChatColor.WHITE + "Min Spawn Delay: " + spawner.getMinSpawnDelay()), ChatColor.WHITE + "Min Spawn Delay: " + argument);
			spawner.setMinSpawnDelay(argument);
		case "activationrange":
			String name = item.getItemMeta().getDisplayName();
			name.replaceAll(" r=" + spawner.getRequiredPlayerRange(), " r=" + argument);
			spawner.setRequiredPlayerRange(argument);
		case "spawncount":
			lore.set(lore.indexOf(ChatColor.WHITE + "Spawn Count: " + spawner.getSpawnCount()), ChatColor.WHITE + "Spawn Count: " + argument);
			spawner.setSpawnCount(argument);
		case "spawnrange":
			lore.set(lore.indexOf(ChatColor.WHITE + "Spawn Range: " + spawner.getSpawnRange()), ChatColor.WHITE + "Spawn Range: " + argument);
			spawner.setSpawnRange(argument);
		}

		meta.setBlockState(spawner);
		item.setItemMeta(meta);
		item.setLore(lore);
	}
}
