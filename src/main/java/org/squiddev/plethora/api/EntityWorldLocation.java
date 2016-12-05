package org.squiddev.plethora.api;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * A world position for an entity
 */
public class EntityWorldLocation implements IWorldLocation {
	private final Entity entity;

	public EntityWorldLocation(@Nonnull Entity entity) {
		Preconditions.checkNotNull(entity, "entity cannot be null");
		this.entity = entity;
	}

	@Nonnull
	@Override
	public World getWorld() {
		return entity.getEntityWorld();
	}

	@Nonnull
	@Override
	public BlockPos getPos() {
		return entity.getPosition();
	}

	@Nonnull
	@Override
	public Vec3 getLoc() {
		return entity.getPositionVector();
	}

	@Nonnull
	@Override
	public IWorldLocation get() throws LuaException {
		return this;
	}
}
