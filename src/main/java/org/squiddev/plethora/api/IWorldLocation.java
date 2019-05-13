package org.squiddev.plethora.api;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * The location within a world.
 * This is exposed in method contexts from a tile entity
 *
 * @see org.squiddev.plethora.api.method.IContext
 */
public interface IWorldLocation extends IReference<IWorldLocation> {
	/**
	 * Get the world this location refers to.
	 *
	 * @return The world for for this location.
	 */
	@Nonnull
	World getWorld();

	/**
	 * Get the block position of this object.
	 *
	 * This will be the block's position for blocks and nearest block for entities.
	 *
	 * @return The position for this location
	 */
	@Nonnull
	BlockPos getPos();

	/**
	 * Get the position vector for this location.
	 *
	 * This will be the centre of the block for blocks.
	 *
	 * @return The decimal position for this location
	 */
	@Nonnull
	Vec3d getLoc();

	/**
	 * Get the bounding box of this location.
	 *
	 * This represents the entire area this location represents. Both {@link #getPos()} and {@link #getLoc()} should be
	 * contained within it.
	 *
	 * @return This location's bounding box.
	 */
	@Nonnull
	AxisAlignedBB getBounds();
}
