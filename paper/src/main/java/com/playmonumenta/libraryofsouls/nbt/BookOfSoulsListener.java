package com.playmonumenta.libraryofsouls.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BookOfSoulsListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onInteractAtEntity(final PlayerInteractAtEntityEvent event) {
		if (event.getHand() == EquipmentSlot.OFF_HAND) {
			return;
		}
		final var player = event.getPlayer();
		final var item = player.getInventory().getItemInMainHand();
		if (!BookOfSouls.isValidEmptyBook(item)) {
			return;
		}
		if (!(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
			// TODO: add audit log that player has item that shouldn't???
			return;
		}
		event.setCancelled(true);
		player.getInventory().addItem(new BookOfSouls(event.getRightClicked()).getBook());
	}



	@EventHandler(priority = EventPriority.LOW)
	public void onLeftClick(PlayerInteractEvent event) {
		final var action = event.getAction();
		if (!action.isLeftClick() || event.getHand() == EquipmentSlot.OFF_HAND) {
			return;
		}
		final var item = event.getItem();
		if (!BookOfSouls.isValidBook(item)) {
			return;
		}
		final var player = event.getPlayer();
		if (!(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
			// TODO: add audit log that player has item that shouldn't???
			return;
		}
		BookOfSouls bos = BookOfSouls.getFromBook(item);
		if (bos == null) {
			player.sendMessage(Component.text("That Book of Souls is corrupted!", NamedTextColor.DARK_RED));
			return;
		}

		Location location = null;
		switch (action) {
			case LEFT_CLICK_BLOCK: {
				Block block = event.getClickedBlock();
				if (block != null && block.getType() != Material.AIR) {
					location = block.getLocation().add(event.getBlockFace().getDirection());
				}
				break;
			}
			case LEFT_CLICK_AIR: {
				Block block = player.getTargetBlockExact(50);
				if (block != null && block.getType() != Material.AIR) {
					location = block.getLocation().add(event.getBlockFace().getDirection()).add(0.0d, 0.3d, 0.0d);
				}
				break;
			}
			default: {
				// Probably should never happen
				return;
			}
		}

		if (location != null) {
			EntityNBTUtils.getFakeEntitySnapshot(bos.getEntityNBT()).createEntity(location);
			event.setCancelled(true);
		} else {
			player.sendMessage("§cNo block in sight!");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDispense(BlockDispenseEvent event) {
		final var item = event.getItem();
		if (!BookOfSouls.isValidBook(item)) {
			return;
		}
		event.setCancelled(true);
		BookOfSouls bos = BookOfSouls.getFromBook(event.getItem());
		if (bos != null) {
			EntityNBTUtils.getFakeEntitySnapshot(bos.getEntityNBT()).createEntity(getLocation(event.getBlock()));
		}
	}
	private Location getLocation(Block block) {
		final var data = block.getBlockData();
		if (data instanceof Directional directional) {
			final var face = directional.getFacing();
			return block.getLocation().add(face.getDirection());
		}
		// Fallback?
		return block.getLocation();
		// if (_location == null) {
		// 	BlockFace face = ((Dispenser) _block.getState().getData()).getFacing();
		// 	_location = _block.getLocation().add(UtilsMc.faceToDelta(face, 0.2)).add(0, -0.3, 0);
		// }
		// return _location;
	}
}
