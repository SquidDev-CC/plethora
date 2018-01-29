package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(ICapabilityProvider.class)
public class ConverterItemHandler extends DynamicConverter<ICapabilityProvider, IItemHandler> {
	@Nullable
	@Override
	public IItemHandler convert(@Nonnull ICapabilityProvider from) {
		return from.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
			? from.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
			: null;
	}
}
