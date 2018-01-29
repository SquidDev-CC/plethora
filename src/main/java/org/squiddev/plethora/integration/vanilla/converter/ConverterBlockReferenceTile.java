package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.reference.BlockReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(BlockReference.class)
public class ConverterBlockReferenceTile extends ConstantConverter<BlockReference, TileEntity> {
	@Nullable
	@Override
	public TileEntity convert(@Nonnull BlockReference from) {
		return from.getTileEntity();
	}
}
