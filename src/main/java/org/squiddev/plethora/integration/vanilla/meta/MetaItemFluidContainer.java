package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Map;

/**
 * Displays fluids contained inside a container
 */
@MetaProvider(value = ItemStack.class, namespace = "fluid")
public class MetaItemFluidContainer implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
		int capacity = 0;

		if (fluidStack == null) {
			Item item = stack.getItem();
			if (item instanceof IFluidContainerItem) {
				IFluidContainerItem container = (IFluidContainerItem) item;
				fluidStack = container.getFluid(stack);
				capacity = container.getCapacity(stack);
			}
		} else {
			capacity = FluidContainerRegistry.getContainerCapacity(fluidStack, stack);
		}

		if (fluidStack != null) {
			Map<Object, Object> data = Maps.newHashMap();
			data.put("capacity", capacity);
			data.put("fluid", PlethoraAPI.instance().metaRegistry().getMeta(fluidStack));
			return data;
		} else {
			return null;
		}
	}
}
