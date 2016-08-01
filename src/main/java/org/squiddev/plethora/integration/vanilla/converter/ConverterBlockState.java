package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;

@IConverter.Inject(IBlockState.class)
public class ConverterBlockState implements IConverter<IBlockState, Block> {
	@Nonnull
	@Override
	public Block convert(@Nonnull IBlockState from) {
		return from.getBlock();
	}
}
