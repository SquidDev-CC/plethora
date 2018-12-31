package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;

/**
 * Provide access to a turtle's inventory via an introspection module
 */
public class MethodsIntrospectionTurtle {
	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = ITurtleAccess.class, worldThread = false,
		doc = "function():table -- Get this turtle's inventory"
	)
	public static Object[] getInventory(ITurtleAccess turtle, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) {
		IItemHandler inventory = turtle.getItemHandler();
		return new Object[]{context.makeChildId(inventory).getObject()};
	}
}
