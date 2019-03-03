package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects
public final class ConverterBlockState extends ConstantConverter<IBlockState, Block> {
	@Nullable
	@Override
	public Block convert(@Nonnull IBlockState from) {
		return from.getBlock();
	}
}
