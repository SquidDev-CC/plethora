package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(IBlockState.class)
public class ConverterBlockState extends ConstantConverter<IBlockState, Block> {
	@Nullable
	@Override
	public Block convert(@Nonnull IBlockState from) {
		return from.getBlock();
	}
}
