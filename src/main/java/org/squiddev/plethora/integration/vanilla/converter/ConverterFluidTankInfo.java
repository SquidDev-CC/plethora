package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(FluidTankInfo.class)
public class ConverterFluidTankInfo implements IConverter<FluidTankInfo, FluidStack> {
	@Nullable
	@Override
	public FluidStack convert(@Nonnull FluidTankInfo from) {
		return from.fluid;
	}
}
