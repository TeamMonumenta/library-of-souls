/*
 * Copyright (C) 2013-2018 Gonçalo Baltazar <me@goncalomb.com>
 *
 * Ported from NBTEditor (https://github.com/goncalomb/NBTEditor) into
 * Library of Souls. Original package: com.goncalomb.bukkit.mylib.utils.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.playmonumenta.libraryofsouls.utils;

import java.util.HashMap;
import java.util.HashSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public abstract class CustomInventory {

	private static @Nullable Listener mMainListener;
	private static @Nullable Plugin mListenerPlugin;
	private static HashMap<HumanEntity, CustomInventory> mOpenedInvsByPlayer = new HashMap<>();
	private static HashMap<Plugin, HashSet<CustomInventory>> mOpenedInvsByPlugin = new HashMap<>();

	private @Nullable Plugin mOwner;
	protected final Inventory mInventory;

	private static void bindListener(Plugin plugin) {
		if (mListenerPlugin == null) {
			if (mMainListener == null) {
				mMainListener = new Listener() {

					@SuppressWarnings("UnusedMethod")
					@EventHandler(ignoreCancelled = true)
					public void pluginDisable(PluginDisableEvent event) {
						HashSet<CustomInventory> invs = mOpenedInvsByPlugin.remove(event.getPlugin());
						if (invs != null) {
							for (CustomInventory inv : invs) {
								for (HumanEntity human : inv.mInventory.getViewers().toArray(new HumanEntity[0])) {
									mOpenedInvsByPlayer.remove(human);
									human.closeInventory();
								}
							}
						}

						if (mListenerPlugin == event.getPlugin()) {
							mListenerPlugin = null;
							HandlerList.unregisterAll(mMainListener);
							if (!mOpenedInvsByPlugin.isEmpty()) {
								bindListener(mOpenedInvsByPlugin.keySet().iterator().next());
							}
						}
					}

					@SuppressWarnings("UnusedMethod")
					@EventHandler(ignoreCancelled = true)
					public void inventoryClick(InventoryClickEvent event) {
						CustomInventory inv = mOpenedInvsByPlayer.get(event.getWhoClicked());
						if (inv != null) {
							inv.inventoryClick(event);
						}
					}

					@SuppressWarnings("UnusedMethod")
					@EventHandler(ignoreCancelled = true)
					public void inventoryDrag(InventoryDragEvent event) {
						CustomInventory inv = mOpenedInvsByPlayer.get(event.getWhoClicked());
						if (inv != null) {
							inv.inventoryDrag(event);
						}
					}

					@SuppressWarnings("UnusedMethod")
					@EventHandler(ignoreCancelled = true)
					public void inventoryClose(InventoryCloseEvent event) {
						CustomInventory inv = mOpenedInvsByPlayer.remove(event.getPlayer());
						if (inv != null) {
							HashSet<CustomInventory> set = mOpenedInvsByPlugin.get(inv.mOwner);
							if (set != null) {
								set.remove(inv);
							}
							inv.inventoryClose(event);
						}
					}

				};
			}

			Bukkit.getPluginManager().registerEvents(mMainListener, plugin);
			mListenerPlugin = plugin;
		}
	}

	public CustomInventory(Player owner, int size) {
		mInventory = Bukkit.createInventory(owner, size);
	}

	public CustomInventory(Player owner, int size, Component title) {
		mInventory = Bukkit.createInventory(owner, size, title);
	}

	public final void openInventory(Player player, Plugin owner) {
		if (mOwner == null) {
			player.openInventory(mInventory);
			mOpenedInvsByPlayer.put(player, this);

			this.mOwner = owner;

			HashSet<CustomInventory> set = mOpenedInvsByPlugin.get(owner);
			if (set == null) {
				set = new HashSet<>();
				mOpenedInvsByPlugin.put(owner, set);
			}
			set.add(this);
			bindListener(owner);
		}
	}

	public Inventory getInventory() {
		return mInventory;
	}

	public @Nullable Plugin getPlugin() {
		return mOwner;
	}

	public final void close() {
		if (mOwner != null) {
			for (HumanEntity human : mInventory.getViewers().toArray(new HumanEntity[0])) {
				human.closeInventory();
			}
		}
	}

	protected void inventoryDrag(InventoryDragEvent event) {
	}

	protected void inventoryClick(InventoryClickEvent event) {
	}

	protected void inventoryClose(InventoryCloseEvent event) {
	}

}
