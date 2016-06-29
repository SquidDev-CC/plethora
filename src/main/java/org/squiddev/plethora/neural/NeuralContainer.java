package org.squiddev.plethora.neural;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.registry.Registry;

public class NeuralContainer extends Container {
	private static final int SLOT = 3;

	private final ItemStack stack;

	public NeuralContainer(ItemStack stack) {
		this.stack = stack.copy();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player != null && player.isEntityAlive() && stack == player.getCurrentArmor(SLOT);
	}

	public ItemStack getStack() {
		return stack;
	}

	public static ItemStack getStack(EntityLivingBase entity) {
		ItemStack stack = entity.getCurrentArmor(SLOT);

		if (stack != null && stack.getItem() == Registry.itemNeuralInterface) {
			return stack;
		} else {
			return null;
		}
	}

	public static NeuralContainer getContainer(EntityLivingBase entity) {
		ItemStack stack = getStack(entity);
		return stack == null ? null : new NeuralContainer(stack);
	}

}
