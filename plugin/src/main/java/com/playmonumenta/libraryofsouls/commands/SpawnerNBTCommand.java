package com.playmonumenta.libraryofsouls.commands;

import java.util.LinkedHashMap;

import com.playmonumenta.libraryofsouls.SpawnerInventory;

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
		arguments.put("value", new IntegerArgument());

		CommandAPI.getInstance().register("nbtheldspawner", perms, arguments, (sender, args) -> {
			changeSpawnerNBT(method, (Integer)args[0], (Player)sender);
		});
	}

	public static void register() {
		registerType("MaxSpawnDelay");
		registerType("MinSpawnDelay");
		registerType("RequiredPlayerRange");
		registerType("SpawnCount");
		registerType("SpawnRange");
	}

	private static void changeSpawnerNBT(String method, int argument, Player player) throws WrapperCommandSyntaxException {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (!item.getType().equals(Material.SPAWNER)) {
			return;
		}
		BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
		CreatureSpawner spawner = (CreatureSpawner)meta.getBlockState();
		switch (method) {
		case "MaxSpawnDelay":
			if (argument < spawner.getMinSpawnDelay()) {
				CommandAPI.fail("Maximum Spawn Delay cannot be smaller than Minimum Spawn Delay!");
				return;
			}
			spawner.setMaxSpawnDelay(argument);
			break;
		case "MinSpawnDelay":
			if (argument > spawner.getMaxSpawnDelay()) {
				CommandAPI.fail("Minimum Spawn Delay cannot be larger than Maximum Spawn Delay!");
				return;
			}
			spawner.setMinSpawnDelay(argument);
			break;
		case "RequiredPlayerRange":
			spawner.setRequiredPlayerRange(argument);
			break;
		case "SpawnCount":
			spawner.setSpawnCount(argument);
			break;
		case "SpawnRange":
			spawner.setSpawnRange(argument);
			break;
		default:
			break;
		}

		meta.setBlockState(spawner);
		item.setItemMeta(meta);

		SpawnerInventory.updateSpawnerItemDisplay(item, spawner);

		player.sendMessage(ChatColor.GREEN + method + " set to " + Integer.toString(argument));
	}
}
