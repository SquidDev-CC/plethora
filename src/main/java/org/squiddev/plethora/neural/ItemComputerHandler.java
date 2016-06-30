package org.squiddev.plethora.neural;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static org.squiddev.plethora.ItemBase.getTag;

/**
 * Attempt to get computer's from items
 */
public final class ItemComputerHandler {
	public static final int WIDTH = 39;
	public static final int HEIGHT = 13;

	private static final String SESSION_ID = "session_id";
	private static final String INSTANCE_ID = "instance_id";
	public static final String COMPUTER_ID = "id";
	public static final String DIRTY = "dirty";
	public static final String ENTITY_MOST = "owner_most";
	public static final String ENTITY_LEAST = "owner_least";

	public static ServerComputer getServer(ItemStack stack, Entity owner, IInventory inventory) {
		NBTTagCompound tag = getTag(stack);

		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();

		ServerComputer computer;
		if (tag.getInteger(SESSION_ID) == sessionId && tag.hasKey(INSTANCE_ID) && manager.contains(tag.getInteger(INSTANCE_ID))) {
			computer = manager.get(tag.getInteger(INSTANCE_ID));
		} else {
			int instanceId = manager.getUnusedInstanceID();

			int computerId;
			if (tag.hasKey(COMPUTER_ID)) {
				computerId = tag.getInteger(COMPUTER_ID);
			} else {
				computerId = ComputerCraft.createUniqueNumberedSaveDir(owner.worldObj, "computer");
			}

			String label = stack.hasDisplayName() ? stack.getDisplayName() : null;
			computer = new ServerComputer(owner.worldObj, computerId, label, instanceId, ComputerFamily.Advanced, WIDTH, HEIGHT);
			manager.add(instanceId, computer);

			setEntity(stack, computer, owner);

			tag.setInteger(SESSION_ID, sessionId);
			tag.setInteger(INSTANCE_ID, instanceId);
			tag.setInteger(COMPUTER_ID, computerId);
			tag.setByte(DIRTY, (byte) 0);

			if (inventory != null) inventory.markDirty();
		}

		computer.setWorld(owner.worldObj);

		return computer;
	}

	public static void setEntity(ItemStack stack, ServerComputer computer, Entity owner) {
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int slot = 0; slot < NeuralHelpers.INV_SIZE; slot++) {
			computer.setPeripheral(slot, NeuralHelpers.buildPeripheral(handler.getStackInSlot(slot), owner));
		}

		NBTTagCompound tag = getTag(stack);
		tag.setLong(ENTITY_LEAST, owner.getPersistentID().getLeastSignificantBits());
		tag.setLong(ENTITY_MOST, owner.getPersistentID().getLeastSignificantBits());
		tag.setByte(DIRTY, (byte) 0);
	}

	public static ServerComputer tryGetServer(ItemStack stack) {
		NBTTagCompound tag = getTag(stack);

		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();

		if (tag.getInteger(SESSION_ID) == sessionId && tag.hasKey(INSTANCE_ID) && manager.contains(tag.getInteger(INSTANCE_ID))) {
			return manager.get(tag.getInteger(INSTANCE_ID));
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
