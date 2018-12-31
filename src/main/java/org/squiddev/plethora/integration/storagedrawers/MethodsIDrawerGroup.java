package org.squiddev.plethora.integration.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.*;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

public class MethodsIDrawerGroup {
	@BasicObjectMethod.Inject(
		value = IDrawerGroup.class, modId = StorageDrawers.MOD_ID,
		doc = "function():int -- Return the number of drawers inside this draw group"
	)
	public static Object[] getDrawerCount(IContext<IDrawerGroup> context, Object[] arguments) {
		return new Object[]{context.getTarget().getDrawerCount()};
	}

	@BasicMethod.Inject(
		value = IDrawerGroup.class, modId = StorageDrawers.MOD_ID,
		doc = "function(slot:int):table -- Return the drawer at this particular slot"
	)
	public static MethodResult getDrawer(final IUnbakedContext<IDrawerGroup> context, Object[] args) throws LuaException {
		final int slot = getInt(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IDrawerGroup> baked = context.bake();
			IDrawerGroup group = baked.getTarget();

			ArgumentHelper.assertBetween(slot, 1, group.getDrawerCount(), "Index out of range (%s)");

			IDrawer drawer = group.getDrawer(slot - 1);
			if (drawer == null) return MethodResult.empty();

			return MethodResult.result(baked.makeChildId(drawer).getObject());
		});
	}
}
