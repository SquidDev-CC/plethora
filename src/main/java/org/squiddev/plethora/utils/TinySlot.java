package org.squiddev.plethora.utils;

import com.google.common.base.Preconditions;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TinySlot {
	private final IInventory inventory;
	private final ItemStack stack;

	public TinySlot(ItemStack stack, IInventory inventory) {
		Preconditions.checkNotNull(stack, "stack cannot be null");

		this.inventory = inventory;
		this.stack = stack;
	}

	public TinySlot(ItemStack stack) {
		this(stack, null);
	}

	@Nonnull
	public ItemStack getStack() {
		return stack;
	}

	public void markDirty() {
		if (inventory != null) inventory.markDirty();
	}

	@Nullable
	public IInventory getInventory() {
		return inventory;
	}
}
