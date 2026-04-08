package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.logging.Logger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntitySnapshot;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public @Nullable ReadWriteNBT getDefaultEntityNBT(EntityType type) {
		var level = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		var key = new ResourceLocation(type.getKey().getNamespace(), type.getKey().getKey());
		var nmsType = BuiltInRegistries.ENTITY_TYPE.get(key);
		if (nmsType == null) {
			return null;
		}
		var entity = nmsType.create(level);
		if (entity == null) {
			return null;
		}
		var compound = new CompoundTag();
		entity.save(compound);
		return NBT.wrapNMSTag(compound);
	}
}
