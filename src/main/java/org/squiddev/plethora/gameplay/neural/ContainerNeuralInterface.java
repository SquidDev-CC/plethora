package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import org.squiddev.plethora.utils.Vec2i;

import javax.annotation.Nullable;

public class ContainerNeuralInterface extends Container implements IContainerComputer {
	public static final int START_Y = 134;

	private static final int MAIN_START_X = 8;
	public static final int NEURAL_START_X = 185;

	public static final int S = 18;

	public static final Vec2i POSITIONS[] = new Vec2i[]{
		new Vec2i(NEURAL_START_X + 1 + S, START_Y + 1 + 2 * S),
		new Vec2i(NEURAL_START_X + 1 + S, START_Y + 1),

		// Centre
		new Vec2i(NEURAL_START_X + 1 + S, START_Y + 1 + S),

		new Vec2i(NEURAL_START_X + 1 + 2 * S, START_Y + 1 + S),
		new Vec2i(NEURAL_START_X + 1, START_Y + 1 + S),
	};

	public static final Vec2i SWAP = new Vec2i(NEURAL_START_X + 1 + 2 * S, START_Y + 1 + 2 * S);

	private final ItemStack stack;
	private final EntityLivingBase parent;

	public final Slot[] peripheralSlots;
	public final Slot[] moduleSlots;

	public ContainerNeuralInterface(IInventory playerInventory, EntityLivingBase parent, ItemStack stack) {
		this.stack = stack;
		this.parent = parent;

		NeuralItemHandler stackInv = (NeuralItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		peripheralSlots = addSlots(stackInv, 0, NeuralHelpers.PERIPHERAL_SIZE);
		moduleSlots = addSlots(stackInv, NeuralHelpers.PERIPHERAL_SIZE, NeuralHelpers.MODULE_SIZE);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, MAIN_START_X + x * 18, START_Y + 1 + y * 18));
			}
		}

		for (int x = 0; x < 9; ++x) {
			addSlotToContainer(new Slot(playerInventory, x, MAIN_START_X + x * 18, START_Y + 54 + 5));
		}
	}

	private Slot[] addSlots(NeuralItemHandler stackInv, int offset, int length) {
		Slot[] slots = new Slot[length];
		for (int i = 0; i < length; i++) {
			addSlotToContainer(slots[i] = new NeuralSlot(stackInv, offset + i, 0, 0));
		}
		return slots;
	}

	@Override
	public boolean canInteractWith(@Nullable EntityPlayer player) {
		return player != null && player.isEntityAlive() && parent.isEntityAlive() && stack == NeuralHelpers.getStack(parent);
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIdx) {
		ItemStack stack = null;
		Slot slot = inventorySlots.get(slotIdx);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			if (slotIdx < NeuralHelpers.INV_SIZE) {
				if (!mergeItemStack(slotStack, NeuralHelpers.INV_SIZE, inventorySlots.size(), true)) {
					return null;
				}
			} else if (!mergeItemStack(slotStack, 0, NeuralHelpers.INV_SIZE, false)) {
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

	/**
	 * Merges provided ItemStack with the first available one in the container/player inventor between minIndex
	 * (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, reverseDirection.
	 *
	 * @see net.minecraftforge.items.ItemStackHandler#insertItem(int, ItemStack, boolean)
	 */
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
		boolean flag = false;
		int i = reverseDirection ? endIndex - 1 : startIndex;

		while (stack.stackSize > 0 && (reverseDirection ? i >= startIndex : i < endIndex)) {
			Slot slot = inventorySlots.get(i);
			ItemStack existing = slot.getStack();

			i += reverseDirection ? -1 : 1;

			if (!slot.isItemValid(stack)) continue;

			int limit = slot.getItemStackLimit(stack);
			if (existing != null) {
				if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) continue;
				limit -= existing.stackSize;
			}

			if (limit <= 0) continue;

			boolean reachedLimit = stack.stackSize > limit;

			if (existing == null) {
				slot.putStack(reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack.copy());
			} else {
				existing.stackSize += reachedLimit ? limit : stack.stackSize;
			}
			slot.onSlotChanged();

			stack.stackSize = reachedLimit ? stack.stackSize - limit : 0;
			flag = true;
		}

		return flag;
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		return ItemComputerHandler.tryGetServer(stack);
	}

	private final class NeuralSlot extends SlotItemHandler {
		private final int index;
		private final NeuralItemHandler handler;

		public NeuralSlot(NeuralItemHandler itemHandler, int index, int xPosition, int yPosition) {
			super(itemHandler, index, xPosition, yPosition);
			this.index = index;
			this.handler = itemHandler;
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return stack != null && handler.isItemValid(index, stack);
		}

	}
}
