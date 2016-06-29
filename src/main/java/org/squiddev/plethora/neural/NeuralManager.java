package org.squiddev.plethora.neural;

import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.ItemBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manager for neural interface
 */
public class NeuralManager {
	private static final HashMap<Integer, NeuralInterface> interfaces = Maps.newHashMap();

	public static void setup() {
		interfaces.clear();
	}

	public static void tearDown() {
		for (NeuralInterface neural : interfaces.values()) {
			neural.unload();
		}

		interfaces.clear();
	}

	public static NeuralInterface get(ItemStack stack, EntityLivingBase entity) {
		return get(ItemBase.getTag(stack), entity);
	}

	public static NeuralInterface get(NBTTagCompound tag, EntityLivingBase entity) {
		if (entity.worldObj.isRemote) throw new RuntimeException("Cannot get NeuralInterface on client thread");

		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();

		NeuralInterface neuralInterface;

		if (tag.getInteger(ServerComputerManager.SESSION_ID) != sessionId) {
			// Running on an old session. Update it
			int id = manager.getUnusedInstanceID();
			neuralInterface = new NeuralInterface(entity, id, sessionId, tag);
			synchronized (interfaces) {
				interfaces.put(id, neuralInterface);
			}
		} else {
			synchronized (interfaces) {
				neuralInterface = interfaces.get(tag.getInteger(ServerComputerManager.INSTANCE_ID));

				if (neuralInterface == null) {
					// Doesn't exist. Create it
					int id = manager.getUnusedInstanceID();
					neuralInterface = new NeuralInterface(entity, id, sessionId, tag);
					interfaces.put(id, neuralInterface);
				} else if (neuralInterface.entity != entity) {
					// Wrong player. Destroy and create
					neuralInterface.unload();

					int id = manager.getUnusedInstanceID();
					neuralInterface = new NeuralInterface(entity, id, sessionId, tag);
					interfaces.put(id, neuralInterface);
				}
			}
		}

		return neuralInterface;
	}

	public static NeuralInterface tryGet(NBTTagCompound tag, EntityLivingBase entity) {
		if (entity.worldObj.isRemote) throw new RuntimeException("Cannot get NeuralInterface on client thread");
		if (tag.getInteger(ServerComputerManager.SESSION_ID) == ComputerCraft.serverComputerRegistry.getSessionID()) {
			synchronized (interfaces) {
				return interfaces.get(tag.getInteger(ServerComputerManager.INSTANCE_ID));
			}
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public static ClientComputer getClient(ItemStack stack) {
		return getClient(ItemBase.getTag(stack));
	}

	@SideOnly(Side.CLIENT)
	public static ClientComputer getClient(NBTTagCompound tag) {
		int instanceId = tag.getInteger(ServerComputerManager.INSTANCE_ID);
		if (instanceId < 0) return null;

		if (!ComputerCraft.clientComputerRegistry.contains(instanceId)) {
			ComputerCraft.clientComputerRegistry.add(instanceId, new ClientComputer(instanceId));
		}

		return ComputerCraft.clientComputerRegistry.get(instanceId);
	}

	public static void update() {
		synchronized (interfaces) {
			Iterator<Map.Entry<Integer, NeuralInterface>> iterator = interfaces.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, NeuralInterface> neural = iterator.next();
				ServerComputer computer = neural.getValue().getComputerLazy();

				if (computer == null) {
					// We can do this as the computer should be shutdown/cleaned up anyway
					iterator.remove();
				}
			}
		}
	}
}
