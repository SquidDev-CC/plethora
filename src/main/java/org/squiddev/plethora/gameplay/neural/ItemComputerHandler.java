package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.utils.DebugLogger;

import static org.squiddev.plethora.gameplay.ItemBase.getTag;

/**
 * Attempt to get computers from items
 */
public final class ItemComputerHandler {
	public static final int WIDTH = 39;
	public static final int HEIGHT = 13;

	public static final String SESSION_ID = "session_id";
	public static final String INSTANCE_ID = "instance_id";
	public static final String COMPUTER_ID = "id";
	public static final String ITEMS = "items";
	public static final String DIRTY = "dirty";
	public static final String MODULE_DATA = "module_data";

	public static NeuralComputer getServer(ItemStack stack, EntityLivingBase owner, IInventory inventory) {
		NBTTagCompound tag = getTag(stack);

		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();

		NeuralComputer neural = null;
		if (tag.getInteger(SESSION_ID) == sessionId && tag.hasKey(INSTANCE_ID) && manager.contains(tag.getInteger(INSTANCE_ID))) {
			ServerComputer computer = manager.get(tag.getInteger(INSTANCE_ID));

			if (computer instanceof NeuralComputer) {
				neural = (NeuralComputer) computer;
			} else {
				DebugLogger.error("Computer is not NeuralComputer but " + computer);
			}
		}

		if (neural == null) {
			int instanceId = manager.getUnusedInstanceID();

			int computerId;
			if (tag.hasKey(COMPUTER_ID)) {
				computerId = tag.getInteger(COMPUTER_ID);
			} else {
				computerId = ComputerCraft.createUniqueNumberedSaveDir(owner.worldObj, "computer");
			}

			String label = stack.hasDisplayName() ? stack.getDisplayName() : null;
			neural = new NeuralComputer(owner.worldObj, computerId, label, instanceId);
			neural.readModuleData(tag.getCompoundTag(MODULE_DATA));

			manager.add(instanceId, neural);

			tag.setInteger(SESSION_ID, sessionId);
			tag.setInteger(INSTANCE_ID, instanceId);
			tag.setInteger(COMPUTER_ID, computerId);

			if (inventory != null) inventory.markDirty();
		}

		return neural;
	}


	public static NeuralComputer tryGetServer(ItemStack stack) {
		NBTTagCompound tag = getTag(stack);

		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();

		if (tag.getInteger(SESSION_ID) == sessionId && tag.hasKey(INSTANCE_ID) && manager.contains(tag.getInteger(INSTANCE_ID))) {
			ServerComputer computer = manager.get(tag.getInteger(INSTANCE_ID));
			if (computer instanceof NeuralComputer) {
				return (NeuralComputer) computer;
			} else {
				DebugLogger.error("Computer is not NeuralComputer but " + computer);
				return null;
			}
		} else {
			return null;
		}
	}

	public static ClientComputer getClient(ItemStack stack) {
		NBTTagCompound tag = getTag(stack);
		int instanceId = tag.getInteger(INSTANCE_ID);
		if (instanceId < 0) return null;

		if (!ComputerCraft.clientComputerRegistry.contains(instanceId)) {
			ComputerCraft.clientComputerRegistry.add(instanceId, new ClientComputer(instanceId));
		}

		return ComputerCraft.clientComputerRegistry.get(instanceId);
	}
}
