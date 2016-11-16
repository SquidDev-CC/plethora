package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gets the block state from a tile entity
 */
@IConverter.Inject(TileEntity.class)
public class ConverterTileEntity implements IConverter<TileEntity, IBlockState> {
	@Nullable
	@Override
	public IBlockState convert(@Nonnull TileEntity from) {
		World world = from.getWorld();
		if (world == null) return null;

		BlockPos pos = from.getPos();
		if (pos == null) return null;

		// Double check that the TE is stil there
		if (world.getTileEntity(pos) != from) return null;

		return world.getBlockState(pos);
	}
}
