package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.core.AppEng;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IGridNode.class, modId = AppEng.MOD_ID)
public class ConverterGrid extends DynamicConverter<IGridNode, IGrid> {
	@Nullable
	@Override
	public IGrid convert(@Nonnull IGridNode from) {
		return from.getGrid();
	}
}
