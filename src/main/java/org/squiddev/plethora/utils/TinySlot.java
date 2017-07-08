package org.squiddev.plethora.utils;

import baubles.api.cap.IBaublesItemHandler;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TinySlot {
	private final ItemStack stack;

	public TinySlot(@Nonnull ItemStack stack) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		this.stack = stack;
	}

	@Nonnull
	public ItemStack getStack() {
		return stack;
	}

	public void markDirty() {
	}

	public static class InventorySlot extends TinySlot {
		private final IInventory inventory;

		public InventorySlot(@Nonnull ItemStack stack, @Nonnull IInventory inventory) {
			super(stack);
			this.inventory = inventory;
		}

		@Override
		public void markDirty() {
			inventory.markDirty();
		}
	}

	public static class BaublesSlot extends TinySlot {
		private final IBaublesItemHandler handler;
		private final int slot;

		public BaublesSlot(@Nonnull ItemStack stack, @Nonnull IBaublesItemHandler handler, int slot) {
			super(stack);
			this.handler = handler;
			this.slot = slot;
		}

		@Override
		public void markDirty() {
			handler.setChanged(slot, true);
		}
	}
}
