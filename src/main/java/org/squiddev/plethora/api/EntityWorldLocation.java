package org.squiddev.plethora.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A world position for an entity
 */
public class EntityWorldLocation implements ConstantReference<IWorldLocation>, IWorldLocation {
	private final Entity entity;

	public EntityWorldLocation(@Nonnull Entity entity) {
		Objects.requireNonNull(entity, "entity cannot be null");
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
		return new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
	}

	@Nonnull
	@Override
	public Vec3d getLoc() {
		return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBounds() {
		AxisAlignedBB bounds = entity.getCollisionBoundingBox();
		return bounds == null ? entity.getEntityBoundingBox() : bounds;
	}

	@Nonnull
	@Override
	public IWorldLocation get() {
		return this;
	}

	@Nonnull
	@Override
	public IWorldLocation safeGet() {
		return new WorldLocation(getWorld(), getLoc());
	}
}
