package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(IFluidTank.class)
public class ConverterFluidTank implements IConverter<IFluidTank, FluidTankInfo> {
	@Nullable
	@Override
	public FluidTankInfo convert(@Nonnull IFluidTank from) {
		return from.getInfo();
	}
}
