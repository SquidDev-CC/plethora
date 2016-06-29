package org.squiddev.plethora.neural;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import static org.squiddev.plethora.neural.NeuralHelpers.ARMOR_SLOT;
import static org.squiddev.plethora.neural.NeuralHelpers.INV_SIZE;

public class ContainerNeuralInterface extends Container {
	private static final int START_Y = 134;

	private static final int MAIN_START_X = 8;
	private static final int NEURAL_START_X = 185;

	private static final int S = 18;

	private final ItemStack stack;

	public ContainerNeuralInterface(IInventory playerInventory, ItemStack stack) {
		this.stack = stack;
		IItemHandlerModifiable stackInv = (IItemHandlerModifiable) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		// DOWN, UP, NORTH, SOUTH, WEST, EAST;
		addSlotToContainer(new SlotItemHandler(stackInv, 0, NEURAL_START_X + 1 + S, START_Y + 1 + 2 * S));
		addSlotToContainer(new SlotItemHandler(stackInv, 1, NEURAL_START_X + 1 + S, START_Y + 1));

		// Bottom right
		addSlotToContainer(new SlotItemHandler(stackInv, 2, NEURAL_START_X + 1 + 2 * S, START_Y + 1 + 2 * S));
		// Centre
		addSlotToContainer(new SlotItemHandler(stackInv, 3, NEURAL_START_X + 1 + S, START_Y + 1 + S));

		addSlotToContainer(new SlotItemHandler(stackInv, 4, NEURAL_START_X + 1 + 2 * S, START_Y + 1 + S));
		addSlotToContainer(new SlotItemHandler(stackInv, 5, NEURAL_START_X + 1, START_Y + 1 + S));

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, MAIN_START_X + x * 18, START_Y + 1 + y * 18));
			}
		}

		for (int x = 0; x < 9; ++x) {
			addSlotToContainer(new Slot(playerInventory, x, MAIN_START_X + x * 18, START_Y + 54 + 5));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player != null && player.isEntityAlive() && stack == player.getCurrentArmor(ARMOR_SLOT);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIdx) {
		ItemStack stack = null;
		Slot slot = inventorySlots.get(slotIdx);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			if (slotIdx < INV_SIZE) {
				if (!mergeItemStack(slotStack, INV_SIZE, inventorySlots.size(), true)) {
					return null;
				}
			} else if (!mergeItemStack(slotStack, 0, INV_SIZE, false)) {
				return null;
			}

			if (slotStack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}

		return stack;
	}
}
