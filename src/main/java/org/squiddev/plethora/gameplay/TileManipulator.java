package org.squiddev.plethora.gameplay;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.TileBase;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import static org.squiddev.plethora.gameplay.BlockManipulator.OFFSET;

public class TileManipulator extends TileBase {
	private ItemStack stack;

	// Lazily loaded render options
	private double offset = -1;
	private long tick;

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readDescription(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeDescription(tag);
	}

	@Override
	protected boolean writeDescription(NBTTagCompound tag) {
		if (stack != null) {
			tag.setTag("stack", stack.serializeNBT());
		} else {
			tag.removeTag("stack");
		}
		return true;
	}

	@Override
	protected void readDescription(NBTTagCompound tag) {
		if (tag.hasKey("stack")) {
			stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
		} else {
			stack = null;
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumFacing side) {
		if (side != EnumFacing.UP) return false;
		if (player.worldObj.isRemote) return true;

		ItemStack newStack = player.getHeldItem();
		DebugLogger.debug("Clicked from " + stack + " → " + newStack);
		if (newStack == null && stack != null) {
			if (!player.capabilities.isCreativeMode) {
				Helpers.spawnItemStack(worldObj, pos.getX(), pos.getY() + OFFSET, pos.getZ(), stack);
			}

			stack = null;
			markForUpdate();

			return true;
		} else if (stack == null && newStack != null && newStack.getItem() instanceof IModuleItem) {
			stack = newStack.copy();
			stack.stackSize = 1;

			if (!player.capabilities.isCreativeMode && --newStack.stackSize <= 0) {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}

			markForUpdate();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBroken() {
		if (stack != null) {
			Helpers.spawnItemStack(worldObj, pos.getX(), pos.getY() + OFFSET, pos.getZ(), stack);
		}
	}

	public ItemStack getStack() {
		return stack;
	}

	public double incrementRotation() {
		long tick = ++this.tick;
		double offset = this.offset;
		if (offset < 0) offset = this.offset = Helpers.RANDOM.nextDouble() * 360;

		return tick + offset;
	}
}
