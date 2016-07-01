package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * A slot in an inventory
 */
public class ItemSlot implements IReference<ItemSlot> {
	private final ItemStack stack;
	private final int slot;
	private final int meta;

	@Nonnull
	private final IItemHandler inventory;

	public ItemSlot(@Nonnull IItemHandler inventory, int slot) {
		this.slot = slot;
		this.inventory = inventory;

		stack = inventory.getStackInSlot(slot);
		meta = stack.getItemDamage();
	}

	/**
	 * Replace this item with another. Call {@link #canReplace()} before hand.
	 *
	 * @param newStack The new item
	 */
	public void replace(ItemStack newStack) {
		((IItemHandlerModifiable) inventory).setStackInSlot(slot, newStack);
	}

	/**
	 * If this slot can be replaced
	 *
	 * @return If this slot can be replaced
	 */
	public boolean canReplace() {
		return inventory instanceof IItemHandlerModifiable;
	}

	/**
	 * Extract a number of items from this slot
	 *
	 * @param count Number of items to extract
	 */
	public void extract(int count) {
		inventory.extractItem(slot, count, false);
	}

	/**
	 * Get the stack for this item
	 *
	 * @return The item's stack
	 */
	public ItemStack getStack() {
		return inventory.getStackInSlot(slot);
	}

	@Nonnull
	@Override
	public ItemSlot get() throws LuaException {
		ItemStack newStack = inventory.getStackInSlot(slot);
		if (
			// If the stack has changed
			newStack != stack &&
				// If the item has changed
				(newStack == null || newStack.getItem() != stack.getItem() ||
					// Or the damage level has changed (ignored for swords).
					(!stack.isItemStackDamageable() && meta != newStack.getItemDamage())
				)
			) {
			throw new LuaException("The stack is no longer there");
		}
		return this;
	}
}
