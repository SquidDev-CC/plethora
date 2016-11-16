package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.squiddev.plethora.api.IWorldLocation;

import javax.annotation.Nonnull;

/**
 * An reference to an entity. Ensures it is still alive and within a radius.
 */
public class BoundedEntityReference<T extends Entity> extends EntityReference<T> {
	private final IWorldLocation location;
	private final int radius;

	public BoundedEntityReference(T entity, IWorldLocation location, int radius) {
		super(entity);
		this.location = location;
		this.radius = radius;
	}

	@Nonnull
	@Override
	public T get() throws LuaException {
		T entity = super.get();

		if (entity.worldObj != location.getWorld()) throw new LuaException("The entity has gone");

		BlockPos pos = entity.getPosition().subtract(location.getPos());
		if (
			pos.getX() < -radius || pos.getX() > radius ||
				pos.getY() < -radius || pos.getY() > radius ||
				pos.getZ() < -radius || pos.getZ() > radius
			) {
			throw new LuaException("The entity is out of range");
		}

		return entity;
	}
}
