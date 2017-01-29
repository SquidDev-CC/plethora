package org.squiddev.plethora.gameplay;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.squiddev.plethora.gameplay.client.gui.GuiKeyboard;
import org.squiddev.plethora.gameplay.client.gui.GuiNeuralInterface;
import org.squiddev.plethora.gameplay.neural.ContainerNeuralInterface;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.utils.DebugLogger;

public class GuiHandler implements IGuiHandler {
	private static final int GUI_NEURAL = 101;
	private static final int GUI_KEYBOARD = 102;

	private static final int GUI_FLAG_PLAYER = 0;
	private static final int GUI_FLAG_ENTITY = 1;

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				ContainerNeuralInterface container = getNeuralContainer(player, world, x, y);
				return container == null ? null : new GuiNeuralInterface(container);
			}
			case GUI_KEYBOARD: {
				ServerComputer computer = ComputerCraft.serverComputerRegistry.get(x);
				return computer == null ? null : new GuiKeyboard(computer);
			}
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				return getNeuralContainer(player, world, x, y);
			}
			case GUI_KEYBOARD: {
				ServerComputer computer = ComputerCraft.serverComputerRegistry.get(x);
				return computer == null ? null : new ContainerKeyboard(computer);
			}
		}

		return null;
	}

	private EntityLivingBase getEntity(EntityPlayer player, World world, int flag, int id) {
		switch (flag) {
			case GUI_FLAG_PLAYER:
				return player;
			case GUI_FLAG_ENTITY: {
				Entity entity = world.getEntityByID(id);
				return entity != null && entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
			}
			default:
				DebugLogger.error("Unknown flag " + flag);
				return null;
		}
	}

	private ContainerNeuralInterface getNeuralContainer(EntityPlayer player, World world, int flag, int id) {
		EntityLivingBase entity = getEntity(player, world, flag, id);
		if (entity == null) return null;

		ItemStack stack = NeuralHelpers.getStack(entity);
		if (stack == null) return null;

		return new ContainerNeuralInterface(player.inventory, entity, stack);
	}

	public static void openKeyboard(EntityPlayer player, World world, ServerComputer computer) {
		player.openGui(Plethora.instance, GUI_KEYBOARD, world, computer.getInstanceID(), 0, 0);
	}

	public static void openNeuralPlayer(EntityPlayer player, World world) {
		player.openGui(Plethora.instance, GUI_NEURAL, world, GUI_FLAG_PLAYER, 0, 0);
	}

	public static void openNeuralEntity(EntityPlayer player, World world, EntityLivingBase entity) {
		player.openGui(Plethora.instance, GUI_NEURAL, world, GUI_FLAG_ENTITY, entity.getEntityId(), 0);
	}
}
