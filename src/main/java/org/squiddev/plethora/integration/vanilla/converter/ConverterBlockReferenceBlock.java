package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.reference.BlockReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects
public final class ConverterBlockReferenceBlock extends DynamicConverter<BlockReference, IBlockState> {
	@Nullable
	@Override
	public IBlockState convert(@Nonnull BlockReference from) {
		return from.getState();
	}
}
