package org.squiddev.plethora.api;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * The location within a world.
 * This is exposed in method contexts from a tile entity
 *
 * @see org.squiddev.plethora.api.method.IContext
 */
public final class WorldLocation implements IWorldLocation {
	private final World world;
	private final BlockPos pos;
	private final Vec3d loc;

	public WorldLocation(@Nonnull World world, @Nonnull BlockPos pos) {
		Preconditions.checkNotNull(world, "world cannot be null");
		Preconditions.checkNotNull(pos, "pos cannot be null");

		this.world = world;
		this.pos = pos.toImmutable();
		this.loc = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	public WorldLocation(@Nonnull World world, @Nonnull Vec3d pos) {
		Preconditions.checkNotNull(world, "world cannot be null");
		Preconditions.checkNotNull(pos, "pos cannot be null");

		this.world = world;
		this.pos = new BlockPos(pos.xCoord, pos.yCoord + 0.5, pos.zCoord);
		this.loc = pos;
	}

	public WorldLocation(@Nonnull World world, int x, int y, int z) {
		this(world, new BlockPos(x, y, z));
	}

	@Override
	@Nonnull
	public World getWorld() {
		return world;
	}

	@Override
	@Nonnull
	public BlockPos getPos() {
		return pos;
	}

	@Nonnull
	@Override
	public Vec3d getLoc() {
		return loc;
	}

	@Nonnull
	@Override
	public IWorldLocation get() throws LuaException {
		return this;
	}

	@Nonnull
	@Override
	public IWorldLocation safeGet() throws LuaException {
		return this;
	}
}
