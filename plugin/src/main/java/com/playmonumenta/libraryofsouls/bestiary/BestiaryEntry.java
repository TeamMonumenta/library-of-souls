package com.playmonumenta.libraryofsouls.bestiary;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.variables.NBTVariableContainer;
import com.playmonumenta.libraryofsouls.SoulEntry;

public class BestiaryEntry {
	public static void generateBestiaryEntry(SoulEntry soul) {
		Inventory inventory = Bukkit.createInventory(null, 27, soul.getName());
		double armor = 0;
		double armorToughness = 0;
		double damage = 0;
		NBTVariableContainer[] vars = BookOfSouls.getFromBook(soul.getBoS()).getEntityNBT().getAllVariables();

		boolean offset = vars.length > 3 ? true : false;
		NBTVariableContainer healthContainer;
		NBTVariableContainer equipmentContainer;
		if (offset) {
			healthContainer = vars[1];
			equipmentContainer = vars[2];
		} else {
			healthContainer = vars[0];
			equipmentContainer = vars[1];
		}

		Bukkit.broadcastMessage(healthContainer.getVariable("Health").get());
		Bukkit.broadcastMessage(equipmentContainer.getVariable("ArmorItems").get());
		double health = Double.valueOf(healthContainer.getVariable("Health").get());

	}
}
