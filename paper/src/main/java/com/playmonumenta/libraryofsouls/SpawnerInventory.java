package com.playmonumenta.libraryofsouls;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.SpawnerNBTWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class SpawnerInventory extends CustomInventory {
	private final @Nullable SoulsInventory mGoBackInventory;

	public SpawnerInventory(Player owner, Soul soul, ItemStack spawner, @Nullable SoulsInventory previous) {
		super(owner, 9, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName()));

		mGoBackInventory = previous;
		loadWindow(spawner);
	}

	private void loadWindow(ItemStack spawnerItem) {
		spawnerItem = changeActivationRange(spawnerItem, 10);
		_inventory.setItem(2, spawnerItem);

		spawnerItem = changeActivationRange(spawnerItem, 12);
		_inventory.setItem(4, spawnerItem);

		spawnerItem = changeActivationRange(spawnerItem, 16);
		_inventory.setItem(6, spawnerItem);

		if (mGoBackInventory != null) {
			ItemStack goBackItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			ItemMeta meta = goBackItem.getItemMeta();
			meta.displayName(Component.text("Go Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
			goBackItem.setItemMeta(meta);
			_inventory.setItem(0, goBackItem);
		}

	}

	private ItemStack changeActivationRange(ItemStack spawnerItem, int range) {
		BlockStateMeta spawnerMeta = (BlockStateMeta)spawnerItem.getItemMeta();
		CreatureSpawner spawner = (CreatureSpawner)spawnerMeta.getBlockState();

		// Change the range
		spawner.setRequiredPlayerRange(range);

		// Update the item to pick up the range change
		spawnerMeta.setBlockState(spawner);
		spawnerItem.setItemMeta(spawnerMeta);

		// Update the item's display info
		updateSpawnerItemDisplay(spawnerItem, spawner);

		return spawnerItem;
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
			// Player clicked off the screen
		}

		if (event.getClickedInventory().equals(getInventory()) && event.getSlot() == 0 && mGoBackInventory != null) {
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			new SoulsInventory(mGoBackInventory, player).openInventory(player, LibraryOfSouls.getInstance());
		} else if ((event.getCursor().getType() != Material.AIR && event.getClickedInventory().equals(getInventory()))
		           || (event.isShiftClick() && !event.getClickedInventory().equals(getInventory()))) {
			// Can't place things in the new inventory
			event.setCancelled(true);
		}
	}

	public static void openSpawnerInventory(Soul soul, Player player, @Nullable SoulsInventory previous) {
		BookOfSouls book = BookOfSouls.getFromBook(soul.getBoS());
		EntityNBT nbt = book.getEntityNBT();

		Block block = findSafeAirBlock(player.getLocation());
		if (block == null) {
			player.sendMessage(Component.text("There is no nearby air block to construct the spawner").color(NamedTextColor.RED));
			player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 1);
			return;
		}

		// Temporarily set the found safe air block to a spawner to manipulate
		block.setType(Material.SPAWNER);
		block.setBlockData(Material.SPAWNER.createBlockData());

		// Attach NBTEditor to the spawner
		SpawnerNBTWrapper spawner = new SpawnerNBTWrapper(block);
		SpawnerNBTWrapper.SpawnerEntity entity = new SpawnerNBTWrapper.SpawnerEntity(nbt, 1 /* weight */);

		// Add the mob to the spawner
		spawner.clearEntities();
		spawner.addEntity(entity);
		spawner.save();

		// Get the new state from the spawner block
		CreatureSpawner spawnerBlock = (CreatureSpawner)block.getState();

		// Set the block back to AIR
		block.setType(Material.AIR);

		// Default spawner stats
		if (soul.isElite() || soul.isBoss()) {
			spawnerBlock.setMaxSpawnDelay(1800);
			spawnerBlock.setMinSpawnDelay(1800);
			spawnerBlock.setSpawnCount(1);
		} else {
			spawnerBlock.setMinSpawnDelay(400);
			spawnerBlock.setMaxSpawnDelay(600);
			spawnerBlock.setSpawnCount(4);
		}
		spawnerBlock.setRequiredPlayerRange(12);
		spawnerBlock.setDelay(0);

		// Get this spawner state back into an item
		ItemStack item = new ItemStack(Material.SPAWNER);
		BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
		meta.setBlockState(spawnerBlock);
		meta.displayName(soul.getDisplayName().append(Component.text(" " + nbt.getEntityType().toString().toLowerCase(Locale.ROOT), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
		item.setItemMeta(meta);

		// Update the item's lore/name
		updateSpawnerItemDisplay(item, spawnerBlock);

		// Open a new inventory with some default range options
		new SpawnerInventory(player, soul, item, previous).openInventory(player, LibraryOfSouls.getInstance());
	}

	/* Suppresses an error internal to Adventure in TextReplacementConfig.builder() */
	@SuppressWarnings("NullAway")
	public static void updateSpawnerItemDisplay(ItemStack item, CreatureSpawner spawner) {
		List<Component> loreString = new ArrayList<>();

		loreString.add(Component.text("MinSpawnDelay: " + spawner.getMinSpawnDelay()).color(NamedTextColor.WHITE));
		loreString.add(Component.text("MaxSpawnDelay: " + spawner.getMaxSpawnDelay()).color(NamedTextColor.WHITE));
		loreString.add(Component.text("Spawn Count: " + spawner.getSpawnCount()).color(NamedTextColor.WHITE));
		loreString.add(Component.text("Spawn Range: " + spawner.getSpawnRange()).color(NamedTextColor.WHITE));

		ItemMeta meta = item.getItemMeta();

		// Append the radius to the item name
		Component name = meta.displayName();
		name = name.replaceText(TextReplacementConfig.builder().match(" r=").replacement(Component.empty()).build());
		name = name.append(Component.text(" r=" + spawner.getRequiredPlayerRange()).color(NamedTextColor.GREEN));

		meta.displayName(name);
		meta.lore(loreString);
		item.setItemMeta(meta);
	}

	private static boolean isSafe(Block block) {
		return block.getType().isAir() &&
			block.getRelative(BlockFace.UP).getType().isAir() &&
			block.getRelative(BlockFace.DOWN).getType().isAir() &&
			block.getRelative(BlockFace.NORTH).getType().isAir() &&
			block.getRelative(BlockFace.EAST).getType().isAir() &&
			block.getRelative(BlockFace.SOUTH).getType().isAir() &&
			block.getRelative(BlockFace.WEST).getType().isAir() &&
			block.getLocation().getNearbyEntities(1, 1, 1).isEmpty();
	}

	private static @Nullable Block findSafeAirBlock(Location startSearch) {
		for (int x = 5; x >= -5; x--) {
			for (int z = 5; z >= -5; z--) {
				for (int y = 5; y >= -5; y--) {
					Location loc = startSearch.clone().add(x, y, z);
					if (loc.getBlockY() >= 0 && loc.getBlockY() <= 255) {
						Block block = loc.getBlock();
						if (isSafe(block)) {
							return block;
						}
					}
				}
			}
		}
		return null;
	}
}
