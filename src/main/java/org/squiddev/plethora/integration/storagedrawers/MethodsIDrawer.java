package org.squiddev.plethora.integration.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Map;

public final class MethodsIDrawer {
	private MethodsIDrawer() {
	}

	@PlethoraMethod(modId = StorageDrawers.MOD_ID, doc = "-- The maximum number of items in this drawer.")
	public static int getCapacity(@FromTarget IDrawer drawer) {
		return drawer.getMaxCapacity();
	}

	@PlethoraMethod(modId = StorageDrawers.MOD_ID, doc = "-- The number of items in this drawer.")
	public static int getCount(@FromTarget IDrawer drawer) {
		return drawer.getStoredItemCount();
	}

	@Optional
	@PlethoraMethod(modId = StorageDrawers.MOD_ID, doc = "-- The metadata of the item in this drawer.")
	public static Map<Object, Object> getItemMeta(IContext<IDrawer> context) {
		ItemStack stack = context.getTarget().getStoredItemPrototype();
		return stack.isEmpty() ? null : context.makePartialChild(stack).getMeta();
	}
}
