package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.reference.BlockReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gets a block reference from a tile entity
 */
@IConverter.Inject(TileEntity.class)
public class ConverterTileEntity extends ConstantConverter<TileEntity, BlockReference> {
	@Nullable
	@Override
	public BlockReference convert(@Nonnull TileEntity from) {
		World world = from.getWorld();
		if (world == null) return null;

		BlockPos pos = from.getPos();
		if (pos == null) return null;

		// Double check that the TE is stil there
		if (world.getTileEntity(pos) != from) return null;

		return new BlockReference(new WorldLocation(world, pos), world.getBlockState(pos), from);
	}
}
