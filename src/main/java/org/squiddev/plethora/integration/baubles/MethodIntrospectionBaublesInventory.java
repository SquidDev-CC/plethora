package org.squiddev.plethora.integration.baubles;

import baubles.api.BaublesApi;
import baubles.common.Baubles;
import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;

/**
 * Allows getting the player's baubles inventory
 */
public final class MethodIntrospectionBaublesInventory {
	private MethodIntrospectionBaublesInventory() {
	}

	@PlethoraMethod(
		module = PlethoraModules.INTROSPECTION_S, modId = Baubles.MODID,
		doc = "-- Get this player's baubles inventory"
	)
	public static TypedLuaObject<IItemHandler> getBaubles(IContext<IModuleContainer> context, @FromSubtarget EntityIdentifier.Player player) throws LuaException {
		IItemHandler inventory = BaublesApi.getBaublesHandler(player.getPlayer());
		return context.makeChildId(inventory).getObject();
	}
}
