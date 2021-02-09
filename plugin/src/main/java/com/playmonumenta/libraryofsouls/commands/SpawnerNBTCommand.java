package com.playmonumenta.libraryofsouls.commands;

import java.util.LinkedHashMap;

import com.playmonumenta.libraryofsouls.SpawnerInventory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class SpawnerNBTCommand {

	public static void registerType(String method) {
		CommandPermission perms = CommandPermission.fromString("los.nbtheldspawner");
		List<Argument> arguments = new ArrayList<>();

		arguments.add(new MultiLiteralArgument(method));
		arguments.add(new IntegerArgument("value"));

		new CommandAPICommand("nbtheldspawner")
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				changeSpawnerNBT(method, (Integer)args[1], (Player)sender);
			})
			.register();
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
