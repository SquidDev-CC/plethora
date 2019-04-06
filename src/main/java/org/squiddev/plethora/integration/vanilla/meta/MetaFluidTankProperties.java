package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects
public final class MetaFluidTankProperties extends BasicMetaProvider<IFluidTankProperties> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IFluidTankProperties tank) {
		return Collections.singletonMap("capacity", tank.getCapacity());
	}

	@Nonnull
	@Override
	public IFluidTankProperties getExample() {
		return new FluidTankProperties(FluidRegistry.getFluidStack("water", 1000), 1000);
	}
}
