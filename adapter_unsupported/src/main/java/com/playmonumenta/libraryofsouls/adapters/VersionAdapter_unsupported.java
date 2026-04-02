package com.playmonumenta.libraryofsouls.adapters;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.logging.Logger;
import org.bukkit.entity.EntitySnapshot;

@SuppressWarnings("checkstyle:TypeName")
public class VersionAdapter_unsupported implements VersionAdapter {
	public VersionAdapter_unsupported() {

	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	public VersionAdapter_unsupported(Logger logger) {

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
