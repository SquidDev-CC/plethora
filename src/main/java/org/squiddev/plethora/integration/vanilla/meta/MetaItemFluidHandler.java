package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Displays fluids contained inside a container
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "fluid")
public class MetaItemFluidHandler extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();
		IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
		if (handler == null) return Collections.emptyMap();

		Map<Object, Object> tanks = Maps.newHashMap();
		int i = 0;

		for (IFluidTankProperties tank : handler.getTankProperties()) {
			Map<Object, Object> data = Maps.newHashMap();
			tanks.put(++i, data);

			data.put("capacity", tank.getCapacity());

			FluidStack contents = tank.getContents();
			if (contents != null) data.put("fluid", context.makePartialChild(contents).getMeta());
		}

		return tanks;
	}
}
