package org.squiddev.plethora.integration.tesla;

import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.darkhax.tesla.lib.Constants;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = ICapabilityProvider.class, modId = Constants.MOD_ID)
public class ConverterTeslaHolder extends DynamicConverter<ICapabilityProvider, ITeslaHolder> {
	@Nullable
	@Override
	public ITeslaHolder convert(@Nonnull ICapabilityProvider from) {
		return from.hasCapability(TeslaCapabilities.CAPABILITY_HOLDER, null)
			? from.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, null)
			: null;
	}
}
