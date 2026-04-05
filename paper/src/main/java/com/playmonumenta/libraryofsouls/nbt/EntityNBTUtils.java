package com.playmonumenta.libraryofsouls.nbt;

import com.playmonumenta.libraryofsouls.utils.NmsUtils;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;

public final class EntityNBTUtils {

	private EntityNBTUtils() {
		// NOOP
	}

	public static Optional<EntityType> getEntityType(ReadableNBT nbt) {
		final var id = nbt.getString("id");
		if (id == null) {
			return Optional.empty();
		}
		return getEntityType(id);
	}

	public static Optional<EntityType> getEntityType(String name) {
		name = name.toLowerCase(Locale.ENGLISH);
		if (name.startsWith("minecraft:")) {
			name = name.substring(10);
		}
		return Optional.ofNullable(EntityType.fromName(name));
	}

	public static EntitySnapshot getFakeEntitySnapshot(ReadWriteNBT nbt) {
		return NmsUtils.getVersionAdapter().createEntitySnapshot(nbt);
	}

	public static Entity getFakeEntity(ReadWriteNBT nbt) {
		final var snapshot = getFakeEntitySnapshot(nbt);
		return snapshot.createEntity(Bukkit.getWorlds().get(0));
	}

	public static ReadWriteNBT getNBTFromEntitySnapshot(EntitySnapshot snapshot) {
		return NmsUtils.getVersionAdapter().getNBTFromEntitySnapshot(snapshot);
	}

}
