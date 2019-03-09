package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.gen.FromSubtarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

/**
 * Provide access to a turtle's inventory via an introspection module
 */
public class MethodsIntrospectionTurtle {
	@PlethoraMethod(
		module = PlethoraModules.INTROSPECTION_S, worldThread = false,
		doc = "-- Get this turtle's inventory"
	)
	public static ILuaObject getInventory(IContext<IModuleContainer> context, @FromSubtarget(ContextKeys.ORIGIN) ITurtleAccess turtle) {
		return context.makeChildId((IItemHandler) turtle.getItemHandler()).getObject();
	}
}
