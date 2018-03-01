package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Various introspection modules which rely on Vanilla classes.
 */
public class MethodsIntrospectionEntity {
	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityPlayer.class, worldThread = false,
		doc = "function():table -- Get this player's inventory"
	)
	@Nullable
	public static Object[] getInventory(EntityPlayer player, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IItemHandler inventory = new PlayerMainInvWrapper(player.inventory);
		return new Object[]{context.makeChildId(inventory).getObject()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityLivingBase.class, worldThread = false,
		doc = "function():table -- Get this entity's held item and armor"
	)
	@Nullable
	public static Object[] getEquipment(EntityLivingBase entity, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IItemHandler inventory = new EquipmentInvWrapper(entity);
		return new Object[]{context.makeChildId(inventory).getObject()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityPlayer.class, worldThread = false,
		doc = "function():table -- Get this player's ender chest"
	)
	@Nullable
	public static Object[] getEnder(EntityPlayer player, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		IInventory inventory = player.getInventoryEnderChest();
		return new Object[]{context.makeChildId(inventory).getObject()};
	}
}
