package org.squiddev.plethora.integration.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

public class MethodsIDrawer {
	@BasicObjectMethod.Inject(
		value = IDrawer.class, modId = StorageDrawers.MOD_ID, worldThread = true,
		doc = "function():int -- The maximum number of items in this drawer"
	)
	public static Object[] getCapacity(IContext<IDrawer> context, Object[] arguments) {
		return new Object[]{context.getTarget().getMaxCapacity()};
	}

	@BasicObjectMethod.Inject(
		value = IDrawer.class, modId = StorageDrawers.MOD_ID, worldThread = true,
		doc = "function():int -- The number of items in this drawer"
	)
	public static Object[] getCount(IContext<IDrawer> context, Object[] arguments) {
		return new Object[]{context.getTarget().getStoredItemCount()};
	}

	@BasicObjectMethod.Inject(
		value = IDrawer.class, modId = StorageDrawers.MOD_ID, worldThread = true,
		doc = "function():table -- The metadata of the item in this drawer"
	)
	public static Object[] getItemMeta(IContext<IDrawer> context, Object[] arguments) {
		ItemStack stack = context.getTarget().getStoredItemPrototype();
		return new Object[]{
			stack.isEmpty()
				? null
				: context.makePartialChild(stack).getMeta()
		};
	}
}
