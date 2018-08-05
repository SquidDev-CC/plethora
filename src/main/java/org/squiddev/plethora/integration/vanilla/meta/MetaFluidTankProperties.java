package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(IFluidTankProperties.class)
public class MetaFluidTankProperties extends BasicMetaProvider<IFluidTankProperties> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IFluidTankProperties tank) {
		return Collections.singletonMap("capacity", tank.getCapacity());
	}

	@Nullable
	@Override
	public IFluidTankProperties getExample() {
		return new FluidTankProperties(FluidRegistry.getFluidStack("water", 1000), 1000);
	}
}
