package com.playmonumenta.libraryofsouls;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.goncalomb.bukkit.mylib.namemaps.EntityTypeMap;
import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.mylib.utils.UtilsMc;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.SpawnerNBTWrapper;
import com.playmonumenta.libraryofsouls.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class SoulsInventory extends CustomInventory {
	private List<? extends Soul> mCurrentSlots;

	private final List<? extends Soul> mSouls;
	private int mOffset;
	private boolean mHasPrevPage;
	private boolean mHasNextPage;

	public SoulsInventory(Player owner, List<? extends Soul> souls, String titleModifier) {
		super(owner, 54, "Souls Library" + (titleModifier.isEmpty() ? "" : " " + Utils.hashColor(titleModifier)));

		mSouls = souls;
		mOffset = 0;
		loadWindow();
	}

	private void loadWindow() {
		mCurrentSlots = mSouls.subList(mOffset, Math.min(mSouls.size(), mOffset + 36));

		for (int i = 0; i < 36; i++) {
			if (i < mCurrentSlots.size()) {
				_inventory.setItem(i, mCurrentSlots.get(i).getPlaceholder());
			} else {
				_inventory.setItem(i, null);
			}
		}

		if (mOffset > 0) {
			_inventory.setItem(45, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 36) + "] Previous Page"));
			mHasPrevPage = true;
		} else {
			mHasPrevPage = false;
			_inventory.setItem(45, null);
		}

		if (mCurrentSlots.size() >= 36) {
			_inventory.setItem(53, UtilsMc.newSingleItemStack(Material.ARROW, "[" + Integer.toString(mOffset / 36) + "] Next Page"));
			mHasNextPage = true;
		} else {
			mHasNextPage = false;
			_inventory.setItem(53, null);
		}
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {
		final int slot = event.getRawSlot();
		if (slot >= 0 && slot < 36) {
			if (event.isShiftClick()) {
				event.setCurrentItem(mCurrentSlots.get(slot).getBoS());
				Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						event.setCurrentItem(mCurrentSlots.get(slot).getPlaceholder());
					}
				});
			} else if (event.isRightClick()) {
				//Lots of variables
				BookOfSouls book = BookOfSouls.getFromBook(mCurrentSlots.get(slot).getBoS());
				Player player = (Player)event.getWhoClicked();
				World world = player.getWorld();

				Block block = world.getBlockAt(player.getLocation());
				Material blockMat = block.getType();
				block.setType(Material.SPAWNER);
				CreatureSpawner spawnerBlock = (CreatureSpawner)block.getState();
				block.setBlockData(spawnerBlock.getBlockData());
				boolean isElite = false;

				//This is just nbts but more fun, hopefully
				SpawnerNBTWrapper spawner = new SpawnerNBTWrapper(block);
				@SuppressWarnings("deprecation")
				EntityType entityType = EntityTypeMap.getByName(book.getEntityNBT().getEntityType().getName());
				int weight = 1;
				SpawnerNBTWrapper.SpawnerEntity entity = new SpawnerNBTWrapper.SpawnerEntity(book.getEntityNBT(), weight);
				if (entityType != null && entityType.isAlive()) {
					spawner.clearEntities();
					spawner.addEntity(entity);
					if (entity.entityNBT.getData().getList("Tags") != null) {
						for (Object obj : entity.entityNBT.getData().getList("Tags").getAsArray()) {
							if (obj.equals("Elite")) {
								isElite = true;
							}
						}
					}
					spawner.save();
				}

				//Maybe required. Dont want to mess with it
				block = spawner.getLocation().getBlock();
				block.getState().update();
				spawnerBlock = (CreatureSpawner)block.getState();

				//Defaults!
				if (isElite) {
					spawnerBlock.setMaxSpawnDelay(1800);
					spawnerBlock.setMinSpawnDelay(1800);
					spawnerBlock.setSpawnCount(1);
					spawnerBlock.setRequiredPlayerRange(12);
				} else {
					spawnerBlock.setMinSpawnDelay(200);
					spawnerBlock.setMaxSpawnDelay(500);
					spawnerBlock.setSpawnCount(4);
					spawnerBlock.setRequiredPlayerRange(12);
				}

				//Helpful Info
				List<String> loreString = new ArrayList<>();
				if (entity.entityNBT.getData().getList("Attributes") != null) {
					for (Object obj : entity.entityNBT.getData().getList("Attributes").getAsArray()) {
						loreString.add(ChatColor.WHITE + obj.toString());
					}
				}

				loreString.add(ChatColor.WHITE + "Min Spawn Delay: " + spawnerBlock.getMinSpawnDelay());
				loreString.add(ChatColor.WHITE + "Max Spawn Delay: " + spawnerBlock.getMaxSpawnDelay());
				loreString.add(ChatColor.WHITE + "Spawn Count: " + spawnerBlock.getSpawnCount());
				loreString.add(ChatColor.WHITE + "Spawn Range: " + spawnerBlock.getSpawnRange());

				//The old song and dance to get this back to an item and replace the block
				ItemStack item = new ItemStack(Material.SPAWNER);
				BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
				meta.setBlockState(spawnerBlock);
				meta.setDisplayName(book.getBook().getItemMeta().getDisplayName());
				meta.setLore(loreString);
				item.setItemMeta(meta);
				//Dont be partially submerged in a command block or its never coming back; blockData didnt work so...
				block.setType(blockMat);

				new SpawnerInventory(player, book.getBook().getItemMeta().getDisplayName(), item).openInventory(player, LibraryOfSouls.getInstance());

				event.setCancelled(true);
			} else {
				if (event.getCursor().getType() == Material.AIR) {
					event.getView().setCursor(mCurrentSlots.get(slot).getBoS());
				}
				event.setCancelled(true);
			}
		} else if (slot == 45 && mHasPrevPage) {
			mOffset -= 36;
			loadWindow();
			event.setCancelled(true);
		} else if (slot == 53 && mHasNextPage) {
			mOffset += 36;
			loadWindow();
			event.setCancelled(true);
		} else if (event.getCursor().getType() == Material.AIR) {
			event.setCancelled(true);
		}
	}


	@Override
	protected void inventoryClose(InventoryCloseEvent event) { }
}
