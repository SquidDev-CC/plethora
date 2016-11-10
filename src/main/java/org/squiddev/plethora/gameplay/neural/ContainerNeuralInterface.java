package org.squiddev.plethora.gameplay.neural;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.squiddev.plethora.utils.Vec2i;

public class ContainerNeuralInterface extends Container {
	private static final int START_Y = 134;

	private static final int MAIN_START_X = 8;
	private static final int NEURAL_START_X = 185;

	private static final int S = 18;

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

		IItemHandlerModifiable stackInv = (IItemHandlerModifiable) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

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

	private Slot[] addSlots(IItemHandlerModifiable stackInv, int offset, int length) {
		Slot[] slots = new Slot[length];
		for (int i = 0; i < length; i++) {
			addSlotToContainer(slots[i] = new SlotItemHandler(stackInv, offset + i, 0, 0));
		}
		return slots;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player != null && player.isEntityAlive() && parent.isEntityAlive() && stack == parent.getEquipmentInSlot(NeuralHelpers.ARMOR_SLOT);
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
}
