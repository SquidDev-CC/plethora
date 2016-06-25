package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * A slot in an inventory
 */
public class ItemSlot implements IReference<ItemSlot> {
	private final ItemStack stack;
	private final int slot;
	private final int meta;
	private final IInventory inventory;

	public ItemSlot(IInventory inventory, int slot) {
		this.slot = slot;
		this.inventory = inventory;

		stack = inventory.getStackInSlot(slot);
		meta = stack.getItemDamage();
	}

	/**
	 * Replace this item with another
	 *
	 * @param newStack The new item
	 */
	public void replace(ItemStack newStack) {
		inventory.setInventorySlotContents(slot, newStack);
	}

	/**
	 * Get the stack for this item
	 *
	 * @return The item's stack
	 */
	public ItemStack getStack() {
		return inventory.getStackInSlot(slot);
	}

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
