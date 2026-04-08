package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.Set;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public interface VersionAdapter {

	EntitySnapshot createEntitySnapshot(ReadWriteNBT input);

	ReadWriteNBT getNBTFromEntitySnapshot(EntitySnapshot snapshot);

	/** Returns the full NBT of a freshly-constructed entity of this type.
	 *  Entity is created at NMS level only — never added to any world, no Bukkit events fired.
	 *  Returns null on unsupported server version. */
	@Nullable ReadWriteNBT getDefaultEntityNBT(EntityType type);

	/** Returns the NBT keys a freshly-constructed entity of this type writes on save. */
	default Set<String> getDefaultEntityNBTKeys(EntityType type) {
		ReadWriteNBT nbt = getDefaultEntityNBT(type);
		return nbt != null ? Set.copyOf(nbt.getKeys()) : Set.of();
	}
}
