package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;

import javax.annotation.Nonnull;

/**
 * An reference to an entity. Ensures it is still alive and within a radius.
 */
public class BoundedEntityReference<T extends Entity> extends EntityReference<T> {
	private final IWorldLocation location;
	private final int radius;
	private boolean valid = true;

	BoundedEntityReference(T entity, IWorldLocation location, int radius) {
		super(entity);
		this.location = location;
		this.radius = radius;
	}

	@Nonnull
	@Override
	public T get() throws LuaException {
		T entity = super.get();

		if (entity.getEntityWorld() != location.getWorld()) throw new LuaException("The entity has gone");

		Vec3d pos = entity.getPositionVector().subtract(location.getLoc());
		if (
			pos.x < -radius || pos.x > radius ||
				pos.y < -radius || pos.y > radius ||
				pos.z < -radius || pos.z > radius
		) {
			valid = false;
			throw new LuaException("The entity is out of range");
		}

		valid = true;
		return entity;
	}

	@Nonnull
	@Override
	public T safeGet() throws LuaException {
		T entity = super.safeGet();
		if (!valid) throw new LuaException("The entity is out of range");
		return entity;
	}
}
