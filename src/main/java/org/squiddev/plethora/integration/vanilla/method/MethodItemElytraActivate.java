package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.core.apis.ArgumentHelper.optBoolean;

@Injects
public final class MethodItemElytraActivate extends BasicObjectMethod<ItemSlot> {
	public MethodItemElytraActivate() {
		super("setActive", true, "function([active:boolean]) -- Set whether these elytra are active or not.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ItemSlot> context) {
		ItemSlot slot = context.getTarget();
		ItemStack stack = slot.getStack();

		if (stack == null || stack.getItem() != Items.ELYTRA) return false;

		EntityPlayerMP player = context.getContext(EntityPlayerMP.class);
		return player != null && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == stack;
	}

	@Nullable
	@Override
	public Object[] apply(@Nonnull IContext<ItemSlot> context, @Nonnull Object[] args) throws LuaException {
		boolean enabled = optBoolean(args, 0, true);

		EntityPlayerMP player = context.getContext(EntityPlayerMP.class);
		if (enabled) {
			player.setElytraFlying();
		} else {
			player.clearElytraFlying();
		}

		return null;
	}
}
