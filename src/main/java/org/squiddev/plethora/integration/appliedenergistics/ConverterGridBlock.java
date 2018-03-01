package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridNode;
import appeng.core.AppEng;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IGridNode.class, modId = AppEng.MOD_ID)
public class ConverterGridBlock extends ConstantConverter<IGridNode, IGridBlock> {
	@Nullable
	@Override
	public IGridBlock convert(@Nonnull IGridNode from) {
		return from.getGridBlock();
	}
}
