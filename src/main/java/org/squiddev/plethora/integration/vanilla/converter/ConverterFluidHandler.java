package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects
public final class ConverterFluidHandler extends DynamicConverter<ICapabilityProvider, IFluidHandler> {
	@Nullable
	@Override
	public IFluidHandler convert(@Nonnull ICapabilityProvider from) {
		return from.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
			? from.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
			: null;
	}
}
