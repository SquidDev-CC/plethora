package org.squiddev.plethora.integration.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.Drawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import javax.annotation.Nullable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

public class MethodsIDrawerGroup {
	@PlethoraMethod(modId = StorageDrawers.MOD_ID, doc = "-- Return the number of drawers inside this draw group")
	public static int getDrawerCount(@FromTarget IDrawerGroup drawer) {
		return drawer.getDrawerCount();
	}

	@Nullable
	@PlethoraMethod(modId = StorageDrawers.MOD_ID, doc = "-- Return the drawer at this particular slot")
	public static ILuaObject getDrawer(IContext<IDrawerGroup> context, int slot) throws LuaException {
		IDrawerGroup group = context.getTarget();

		assertBetween(slot, 1, group.getDrawerCount(), "Index out of range (%s)");

		IDrawer drawer = group.getDrawer(slot - 1);
		if (drawer == Drawers.DISABLED) return null;

		return context.makeChildId(drawer).getObject();
	}
}
