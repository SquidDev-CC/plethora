package org.squiddev.plethora.api;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;

/**
 * A world position for an entity
 */
public class EntityWorldLocation extends ConstantReference<IWorldLocation> implements IWorldLocation {
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
		return new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
	}

	@Nonnull
	@Override
	public Vec3d getLoc() {
		return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
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
