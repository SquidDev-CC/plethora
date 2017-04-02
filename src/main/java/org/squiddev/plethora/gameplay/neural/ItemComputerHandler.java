package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.computer.IExtendedServerComputer;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;

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

	public static NeuralComputer getServer(@Nonnull ItemStack stack, EntityLivingBase owner, TinySlot inventory) {
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
				computerId = ComputerCraft.createUniqueNumberedSaveDir(owner.getEntityWorld(), "computer");
			}

			String label = stack.hasDisplayName() ? stack.getDisplayName() : null;
			neural = new NeuralComputer(owner.getEntityWorld(), computerId, label, instanceId);
			neural.readModuleData(tag.getCompoundTag(MODULE_DATA));

			if (Loader.isModLoaded(CCTweaks.ID) && tag.hasKey("rom_id", 99)) {
				((IExtendedServerComputer) neural).setCustomRom(tag.getInteger("rom_id"));
			}

			manager.add(instanceId, neural);

			tag.setInteger(SESSION_ID, sessionId);
			tag.setInteger(INSTANCE_ID, instanceId);
			tag.setInteger(COMPUTER_ID, computerId);

			if (inventory != null) inventory.markDirty();
		}

		return neural;
	}


	public static NeuralComputer tryGetServer(@Nonnull ItemStack stack) {
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

	public static ClientComputer getClient(@Nonnull ItemStack stack) {
		NBTTagCompound tag = getTag(stack);
		int instanceId = tag.getInteger(INSTANCE_ID);
		if (instanceId < 0) return null;

		if (!ComputerCraft.clientComputerRegistry.contains(instanceId)) {
			ComputerCraft.clientComputerRegistry.add(instanceId, new ClientComputer(instanceId));
		}

		return ComputerCraft.clientComputerRegistry.get(instanceId);
	}
}
