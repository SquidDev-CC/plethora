package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Basic properties for fluid stacks
 */
@Injects
public final class MetaFluidStack extends BasicMetaProvider<FluidStack> {
	public MetaFluidStack() {
		super("Provides information about a fluid, as well as how much is currently stored.");
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull FluidStack fluidStack) {
		Map<Object, Object> data = Maps.newHashMap();
		data.put("amount", fluidStack.amount);

		Fluid fluid = fluidStack.getFluid();
		if (fluid != null) {
			data.put("name", fluid.getName());
			data.put("id", FluidRegistry.getDefaultFluidName(fluid));
			data.put("rawName", fluid.getUnlocalizedName(fluidStack));
			data.put("displayName", fluid.getLocalizedName(fluidStack));
		}

		return data;
	}

	@Nullable
	@Override
	public FluidStack getExample() {
		return FluidRegistry.getFluidStack("water", 525);
	}
}
