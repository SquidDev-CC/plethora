package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.optBoolean;

@Injects
public final class MethodItemElytraActivate extends BasicMethod<ItemSlot> {
	public MethodItemElytraActivate() {
		super("setActive", "function([active:boolean]) -- Set whether these elytra are active or not.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ItemSlot> context) {
		ItemSlot slot = context.getTarget();
		ItemStack stack = slot.getStack();

		if (stack.isEmpty() || stack.getItem() != Items.ELYTRA) return false;

		EntityPlayerMP player = context.getContext(EntityPlayerMP.class);
		return player != null && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == stack;
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<ItemSlot> context, @Nonnull Object[] args) throws LuaException {
		boolean enabled = optBoolean(args, 0, true);

		return MethodResult.nextTick(() -> {
			EntityPlayerMP player = context.bake().getContext(EntityPlayerMP.class);
			if (enabled) {
				player.setElytraFlying();
			} else {
				player.clearElytraFlying();
			}

			return MethodResult.empty();
		});
	}
}
