package org.squiddev.plethora;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.squiddev.plethora.client.gui.GuiNeuralInterface;
import org.squiddev.plethora.neural.ContainerNeuralInterface;
import org.squiddev.plethora.neural.NeuralHelpers;
import org.squiddev.plethora.utils.DebugLogger;

public class GuiHandler implements IGuiHandler {
	public static final int GUI_NEURAL = 101;

	public static final int GUI_FLAG_PLAYER = 0;
	public static final int GUI_FLAG_ENTITY = 1;

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				ContainerNeuralInterface container = getNeuralContainer(player, world, x, y);
				return container == null ? null : new GuiNeuralInterface(container);
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
}
