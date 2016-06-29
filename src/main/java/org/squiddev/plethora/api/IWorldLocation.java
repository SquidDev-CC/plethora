package org.squiddev.plethora.api;

import net.minecraft.util.BlockPos;
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
	@Nonnull
	World getWorld();

	@Nonnull
	BlockPos getPos();
}
