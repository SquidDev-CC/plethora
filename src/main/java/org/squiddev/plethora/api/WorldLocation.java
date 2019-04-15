package org.squiddev.plethora.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The location within a world.
 * This is exposed in method contexts from a tile entity
 *
 * @see org.squiddev.plethora.api.method.IContext
 */
public final class WorldLocation implements ConstantReference<IWorldLocation>, IWorldLocation {
	private final World world;
	private final BlockPos pos;
	private final Vec3d loc;

	public WorldLocation(@Nonnull World world, @Nonnull BlockPos pos) {
		Objects.requireNonNull(world, "world cannot be null");
		Objects.requireNonNull(pos, "pos cannot be null");

		this.world = world;
		this.pos = pos.toImmutable();
		loc = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	public WorldLocation(@Nonnull World world, @Nonnull Vec3d pos) {
		Objects.requireNonNull(world, "world cannot be null");
		Objects.requireNonNull(pos, "pos cannot be null");

		this.world = world;
		this.pos = new BlockPos(pos.x, pos.y + 0.5, pos.z);
		loc = pos;
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
	public IWorldLocation get() {
		return this;
	}

	@Nonnull
	@Override
	public IWorldLocation safeGet() {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof WorldLocation)) return false;

		WorldLocation other = (WorldLocation) obj;
		return world.equals(other.world) && pos.equals(other.pos);
	}

	@Override
	public int hashCode() {
		return world.hashCode() * 31 + pos.hashCode();
	}
}
