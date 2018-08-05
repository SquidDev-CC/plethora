package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Displays fluids contained inside a container
 */
@IMetaProvider.Inject(value = IFluidHandler.class, namespace = "tanks")
public class MetaFluidHandler extends BaseMetaProvider<IFluidHandler> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<IFluidHandler> context) {
		IFluidHandler handler = context.getTarget();
		Map<Object, Object> tanks = Maps.newHashMap();
		int i = 0;

		for (IFluidTankProperties tank : handler.getTankProperties()) {
			tanks.put(++i, context.makePartialChild(tank).getMeta());
		}

		return tanks;
	}

	@Nullable
	@Override
	public IFluidHandler getExample() {
		return new FluidHandlerItemStack(new ItemStack(Items.WATER_BUCKET), 1000) {
			FluidStack stack = FluidRegistry.getFluidStack("water", 525);

			@Nullable
			@Override
			public FluidStack getFluid() {
				return stack;
			}
		};
	}
}
