package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.entity.EntitySnapshot;

public interface VersionAdapter {

	EntitySnapshot createEntitySnapshot(ReadWriteNBT input);

	ReadWriteNBT getNBTFromEntitySnapshot(EntitySnapshot snapshot);
}
