package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;
import org.squiddev.plethora.utils.EquipmentInvWrapper;

/**
 * Various introspection modules which rely on Vanilla classes.
 */
public class MethodsIntrospectionEntity {
	@PlethoraMethod(module = PlethoraModules.INTROSPECTION_S, doc = "-- Get this player's inventory")
	public static ILuaObject getInventory(IContext<IModuleContainer> context, @FromSubtarget EntityIdentifier.Player player) throws LuaException {
		IItemHandler inventory = new PlayerMainInvWrapper(player.getPlayer().inventory);
		return context.makeChildId(inventory).getObject();
	}

	@PlethoraMethod(module = PlethoraModules.INTROSPECTION_S, doc = "-- Get this entity's held item and armor")
	public static ILuaObject getEquipment(IContext<IModuleContainer> context, @FromSubtarget EntityIdentifier entity) throws LuaException {
		IItemHandler inventory = new EquipmentInvWrapper(entity.getEntity());
		return context.makeChildId(inventory).getObject();
	}

	@PlethoraMethod(module = PlethoraModules.INTROSPECTION_S, doc = "-- Get this player's ender chest")
	public static ILuaObject getEnder(IContext<IModuleContainer> context, @FromSubtarget EntityIdentifier.Player player) throws LuaException {
		IInventory inventory = player.getPlayer().getInventoryEnderChest();
		return context.makeChildId(inventory).getObject();
	}
}
