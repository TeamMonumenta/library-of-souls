package com.playmonumenta.libraryofsouls.nbt.wrapper;

import dev.jorel.commandapi.CommandAPICommand;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class Wrapper<T extends Entity> {

	public static final Map<Class<?>, Wrapper<?>> entityClasses = new HashMap<>();

	public Wrapper(Class<?> type) {
		entityClasses.put(type, this);
	}

	abstract void getReadCommand(CommandAPICommand command);

	abstract void getWriteCommand(CommandAPICommand command);

	abstract Component getDescription();
}
