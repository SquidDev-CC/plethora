package org.squiddev.plethora.gameplay.neural;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.gameplay.ItemBase;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.api.Constants.*;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.DIRTY;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.ITEMS;

/**
 * A horrible item handler implementation that saves to the item's NBT
 * rather than the capability. See #12
 */
public class NeuralItemHandler implements IItemHandlerModifiable {
	private final ItemStack stack;

	public NeuralItemHandler(ItemStack stack) {
		this.stack = stack;
	}

	private void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= getSlots()) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
		}
	}

	@Override
	public int getSlots() {
		return NeuralHelpers.INV_SIZE;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		if (stack.isEmpty()) return false;

		if (slot < NeuralHelpers.PERIPHERAL_SIZE) {
			return stack.hasCapability(PERIPHERAL_HANDLER_CAPABILITY, null) ||
				stack.hasCapability(PERIPHERAL_CAPABILITY, null);
		} else {
			return stack.hasCapability(MODULE_HANDLER_CAPABILITY, null);
		}
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);

		NBTTagCompound tag = ItemBase.getTag(this.stack);
		NBTTagCompound items = getItems(tag);
		if (stack.isEmpty()) {
			items.removeTag("item" + slot);
		} else {
			items.setTag("item" + slot, stack.serializeNBT());
		}

		tag.setShort(DIRTY, (short) (tag.getShort(DIRTY) | 1 << slot));
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);

		NBTTagCompound tag = ItemBase.getTag(stack);
		NBTTagCompound items = getItems(tag);
		return items.hasKey("item" + slot, Constants.NBT.TAG_COMPOUND) ? new ItemStack(items.getCompoundTag("item" + slot)) : ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !getStackInSlot(slot).isEmpty()) return stack;

		return isItemValid(slot, stack) ? doInsert(slot, stack, simulate) : stack;
	}

	@Nonnull
	private ItemStack doInsert(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);
		int limit = getSlotLimit(slot);

		if (!existing.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
				return stack;
			}

			limit -= existing.getCount();
		}

		if (limit <= 0) return stack;

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate) {
			if (existing.isEmpty()) {
				setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
				setStackInSlot(slot, existing);
			}
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		if (existing.isEmpty()) return ItemStack.EMPTY;

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				setStackInSlot(slot, ItemStack.EMPTY);
			}
			return existing;
		} else {
			if (!simulate) {
				setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
			}

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	private static NBTTagCompound getItems(NBTTagCompound tag) {
		NBTTagCompound items;
		if (tag.hasKey(ITEMS, Constants.NBT.TAG_COMPOUND)) {
			items = tag.getCompoundTag(ITEMS);
		} else {
			tag.setTag(ITEMS, items = new NBTTagCompound());
		}

		return items;
	}

}
