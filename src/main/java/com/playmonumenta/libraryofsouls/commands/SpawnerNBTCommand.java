package com.playmonumenta.libraryofsouls.commands;

import com.playmonumenta.libraryofsouls.SpawnerInventory;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerNBTCommand {

	public static void register() {
		CommandPermission perms = CommandPermission.fromString("los.nbtheldspawner");

		MultiLiteralArgument methodArg = new MultiLiteralArgument("method", "MaxSpawnDelay", "MinSpawnDelay", "RequiredPlayerRange", "SpawnCount", "SpawnRange");
		IntegerArgument valueArg = new IntegerArgument("value");

		new CommandAPICommand("nbtheldspawner")
			.withPermission(perms)
			.withArguments(methodArg)
			.withArguments(valueArg)
			.executesPlayer((sender, args) -> {
				changeSpawnerNBT(args.getByArgument(methodArg), args.getByArgument(valueArg), sender);
			})
			.register();
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
				throw CommandAPI.failWithString("Maximum Spawn Delay cannot be smaller than Minimum Spawn Delay!");
			}
			spawner.setMaxSpawnDelay(argument);
			break;
		case "MinSpawnDelay":
			if (argument > spawner.getMaxSpawnDelay()) {
				throw CommandAPI.failWithString("Minimum Spawn Delay cannot be larger than Maximum Spawn Delay!");
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

		player.sendMessage(Component.text(method + " set to " + argument, NamedTextColor.GREEN));
	}
}
