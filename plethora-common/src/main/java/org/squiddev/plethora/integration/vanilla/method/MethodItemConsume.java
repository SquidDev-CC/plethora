package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;

/**
 * A provider for consumables: food and potions.
 *
 * This enables consuming it: Eating/Drinking it
 */
@Injects
public final class MethodItemConsume extends BasicMethod<ItemSlot> {
	public MethodItemConsume() {
		super("consume", "function() -- Consume one item from this stack");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ItemSlot> context) {
		if (!context.getTarget().canReplace() || !context.hasContext(EntityPlayer.class)) return false;

		ItemStack stack = context.getTarget().getStack();
		if (stack.isEmpty()) return false;

		EnumAction action = stack.getItemUseAction();
		return action == EnumAction.EAT || action == EnumAction.DRINK;
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<ItemSlot> unbaked, @Nonnull Object[] args) {
		return MethodResult.nextTick(() -> {
			IContext<ItemSlot> context = unbaked.bake();
			ItemSlot slot = context.getTarget();
			EntityPlayer player = context.getContext(EntityPlayer.class);

			slot.replace(slot.getStack().onItemUseFinish(player.getEntityWorld(), player));
			return MethodResult.empty();
		});
	}
}
