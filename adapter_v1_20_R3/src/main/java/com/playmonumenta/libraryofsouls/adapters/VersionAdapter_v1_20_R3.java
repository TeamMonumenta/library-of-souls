package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.logging.Logger;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntitySnapshot;
import org.bukkit.entity.EntitySnapshot;

@SuppressWarnings("checkstyle:TypeName")
public class VersionAdapter_v1_20_R3 implements VersionAdapter {

	public VersionAdapter_v1_20_R3(Logger logger) {
	}

	@Override
	public EntitySnapshot createEntitySnapshot(ReadWriteNBT nbt) {
		if (!(nbt instanceof NBTContainer internalNBT)) {
			throw new IllegalArgumentException("Not Internal NBT compound!");
		}
		if (!(internalNBT.getCompound() instanceof CompoundTag mcNbt)) {
			throw new IllegalArgumentException("Not MC compound!");
		}
		final EntitySnapshot snapshot = CraftEntitySnapshot.create(mcNbt);
		if (snapshot == null) {
			throw new IllegalArgumentException("Failed to create entity from MC compound");
		}
		return snapshot;
		// meow?
	}


	@Override
	public ReadWriteNBT getNBTFromEntitySnapshot(EntitySnapshot snapshot) {
		if (!(snapshot instanceof CraftEntitySnapshot cfSnapshot)) {
			throw new IllegalArgumentException("Not CraftEntitySnapshot");
		}

		return NBT.wrapNMSTag(cfSnapshot.getData());
	}
}
