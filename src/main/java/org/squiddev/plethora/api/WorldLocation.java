package org.squiddev.plethora.api;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.BlockPos;
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

	public WorldLocation(@Nonnull World world, @Nonnull BlockPos pos) {
		Preconditions.checkNotNull(world, "world cannot be null");
		Preconditions.checkNotNull(pos, "pos cannot be null");

		this.world = world;
		this.pos = pos.getImmutable();
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
	public IWorldLocation get() throws LuaException {
		return this;
	}
}
