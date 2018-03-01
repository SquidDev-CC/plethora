package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(IFluidTankProperties.class)
public class ConverterFluidTankProperties extends DynamicConverter<IFluidTankProperties, FluidStack> {
	@Nullable
	@Override
	public FluidStack convert(@Nonnull IFluidTankProperties from) {
		return from.getContents();
	}
}
