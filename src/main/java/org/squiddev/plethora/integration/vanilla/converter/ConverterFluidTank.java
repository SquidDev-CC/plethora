package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects
public final class ConverterFluidTank extends DynamicConverter<IFluidTank, FluidStack> {
	@Nullable
	@Override
	public FluidStack convert(@Nonnull IFluidTank from) {
		return from.getFluid();
	}
}
