package com.playmonumenta.libraryofsouls.nbt.wrapper;

import org.bukkit.entity.EntityType;

public class WrapperInit {
	private void loopEntityClasses() {
		for (EntityType type : EntityType.values()) {
			final var clazz = type.getEntityClass();

		}
	}
}
