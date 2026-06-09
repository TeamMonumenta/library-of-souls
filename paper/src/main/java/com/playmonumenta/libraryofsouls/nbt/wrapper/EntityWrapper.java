package com.playmonumenta.libraryofsouls.nbt.wrapper;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;

public class EntityWrapper extends Wrapper<Entity> {

	public EntityWrapper() {
		super(Entity.class);
	}
	@Override
	public void getReadCommand(final CommandAPICommand command) {

	}

	@Override
	public void getWriteCommand(final CommandAPICommand command) {

	}

	@Override
	public Component getDescription() {

	}
}
