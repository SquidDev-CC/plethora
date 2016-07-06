package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;

/**
 * A provider for consumables: food and potions.
 *
 * This enables consuming it: Eating/Drinking it
 */
@Method(ItemSlot.class)
public class MethodItemConsume extends BasicObjectMethod<ItemSlot> {
	public MethodItemConsume() {
		super("consume", true);
	}

	@Override
	public boolean canApply(@Nonnull IContext<ItemSlot> context) {
		if (!context.getTarget().canReplace()) return false;

		ItemStack stack = context.getTarget().getStack();

		if (stack != null && context.hasContext(EntityPlayer.class)) {
			EnumAction action = stack.getItemUseAction();
			if (action == EnumAction.EAT || action == EnumAction.DRINK) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Object[] apply(@Nonnull IContext<ItemSlot> context, @Nonnull Object[] args) throws LuaException {
		ItemSlot slot = context.getTarget();
		ItemStack stack = slot.getStack();
		EntityPlayer player = context.getContext(EntityPlayer.class);

		slot.replace(stack.onItemUseFinish(player.worldObj, player));
		return null;
	}
}
