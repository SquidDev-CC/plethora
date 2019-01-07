package org.squiddev.plethora.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

/**
 * The entity this references
 */
public final class EquipmentInvWrapper implements IItemHandlerModifiable {
	private static final EntityEquipmentSlot[] VALUES = EntityEquipmentSlot.values();
	private static final int SLOTS = VALUES.length;

	private final EntityLivingBase entity;

	public EquipmentInvWrapper(EntityLivingBase entity) {
		this.entity = entity;
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);
		entity.setItemStackToSlot(VALUES[slot], stack);
	}

	@Override
	public int getSlots() {
		return SLOTS;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return entity.getItemStackFromSlot(VALUES[slot]);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!stack.isEmpty()) {
			validateSlotIndex(slot);

			EntityEquipmentSlot slotType = VALUES[slot];
			if (slotType.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !stack.getItem().isValidArmor(stack, slotType, entity)) {
				return stack;
			}

			ItemStack existing = getStackInSlot(slot);
			int limit = stack.getMaxStackSize();
			if (!existing.isEmpty()) {
				if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
					return stack;
				}

				limit -= existing.getCount();
			}

			if (limit <= 0) {
				return stack;
			} else {
				boolean reachedLimit = stack.getCount() > limit;
				if (!simulate) {
					if (existing.isEmpty()) {
						setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
					} else {
						existing.grow(reachedLimit ? limit : stack.getCount());
					}
					onContentsChanged(slot);
				}

				return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}


	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		} else {
			validateSlotIndex(slot);
			ItemStack existing = getStackInSlot(slot);
			if (existing.isEmpty()) {
				return ItemStack.EMPTY;
			} else {
				int toExtract = Math.min(amount, existing.getMaxStackSize());
				if (existing.getCount() <= toExtract) {
					if (!simulate) {
						setStackInSlot(slot, ItemStack.EMPTY);
						onContentsChanged(slot);
					}

					return existing;
				} else {
					if (!simulate) {
						setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
						onContentsChanged(slot);
					}

					return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
				}
			}
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	private void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= SLOTS) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0, " + SLOTS + "]");
		}
	}

	private void onContentsChanged(int slot) {
		if (entity instanceof EntityLiving) {
			((EntityLiving) entity).setDropChance(VALUES[slot], 1.1f);
		} else if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).inventory.markDirty();
		}
	}
}
