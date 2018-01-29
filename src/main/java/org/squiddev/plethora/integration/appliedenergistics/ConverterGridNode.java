package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IGridHost.class, modId = AppEng.MOD_ID)
public class ConverterGridNode extends DynamicConverter<IGridHost, IGridNode> {
	@Nullable
	@Override
	public IGridNode convert(@Nonnull IGridHost from) {
		return from.getGridNode(AEPartLocation.INTERNAL);
	}

	@Nullable
	public static IGridNode findNode(@Nonnull IGridHost host) {
		IGridNode node = host.getGridNode(AEPartLocation.INTERNAL);
		if (node != null) return node;

		for (AEPartLocation location : AEPartLocation.SIDE_LOCATIONS) {
			node = host.getGridNode(location);
			if (node != null) return node;
		}

		return null;
	}
}
