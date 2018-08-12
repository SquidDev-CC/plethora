package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;

import javax.annotation.Nonnull;

/**
 * Various introspection modules which rely on Vanilla classes.
 */
public class MethodsIntrospectionEntity {
	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.Player.class, worldThread = false,
		doc = "function():table -- Get this player's inventory"
	)
	public static Object[] getInventory(EntityIdentifier.Player player, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IItemHandler inventory = new PlayerMainInvWrapper(player.getPlayer().inventory);
		return new Object[]{context.makeChildId(inventory).getObject()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.class, worldThread = false,
		doc = "function():table -- Get this entity's held item and armor"
	)
	public static Object[] getEquipment(EntityIdentifier entity, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IItemHandler inventory = new EquipmentInvWrapper(entity.getEntity());
		return new Object[]{context.makeChildId(inventory).getObject()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.Player.class, worldThread = false,
		doc = "function():table -- Get this player's ender chest"
	)
	public static Object[] getEnder(EntityIdentifier.Player player, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IInventory inventory = player.getPlayer().getInventoryEnderChest();
		return new Object[]{context.makeChildId(inventory).getObject()};
	}
}
