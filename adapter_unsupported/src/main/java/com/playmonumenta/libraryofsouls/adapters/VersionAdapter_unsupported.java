package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.entity.EntitySnapshot;

@SuppressWarnings("checkstyle:TypeName")
public class VersionAdapter_unsupported implements VersionAdapter {
	public VersionAdapter_unsupported() {

	}

	@Override
	public EntitySnapshot createEntitySnapshot(ReadWriteNBT input) {
		return null;
	}

	@Override
	public ReadWriteNBT getNBTFromEntitySnapshot(EntitySnapshot snapshot) {
		return null;
	}
}
