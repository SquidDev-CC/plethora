package org.squiddev.plethora;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * The entity this references
 */
public class EquipmentInvWrapper implements IItemHandlerModifiable {
	private static final int SLOTS = 5;

	private final EntityLivingBase entity;

	public EquipmentInvWrapper(EntityLivingBase entity) {
		this.entity = entity;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		validateSlotIndex(slot);
		entity.setCurrentItemOrArmor(slot, stack);
	}

	@Override
	public int getSlots() {
		return 5;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return entity.getEquipmentInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack != null && stack.stackSize != 0) {
			validateSlotIndex(slot);

			if (slot > 0 && !stack.getItem().isValidArmor(stack, 4 - slot, entity)) {
				return stack;
			}

			ItemStack existing = getStackInSlot(slot);
			int limit = stack.getMaxStackSize();
			if (existing != null) {
				if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
					return stack;
				}

				limit -= existing.stackSize;
			}

			if (limit <= 0) {
				return stack;
			} else {
				boolean reachedLimit = stack.stackSize > limit;
				if (!simulate) {
					if (existing == null) {
						setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
					} else {
						existing.stackSize += reachedLimit ? limit : stack.stackSize;
					}
					onContentsChanged(slot);
				}

				return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
			}
		} else {
			return null;
		}
	}


	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return null;
		} else {
			validateSlotIndex(slot);
			ItemStack existing = getStackInSlot(slot);
			if (existing == null) {
				return null;
			} else {
				int toExtract = Math.min(amount, existing.getMaxStackSize());
				if (existing.stackSize <= toExtract) {
					if (!simulate) {
						setStackInSlot(slot, null);
						onContentsChanged(slot);
					}

					return existing;
				} else {
					if (!simulate) {
						setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
						onContentsChanged(slot);
					}

					return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
				}
			}
		}
	}

	private void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= SLOTS) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0, " + SLOTS + "]");
		}
	}

	private void onContentsChanged(int slot) {
		if (entity instanceof EntityLiving) {
			((EntityLiving) entity).setEquipmentDropChance(slot, 1.1f);
		} else if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).inventory.markDirty();
		}
	}
}
