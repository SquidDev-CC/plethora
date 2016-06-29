package org.squiddev.plethora;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.squiddev.plethora.client.gui.GuiNeuralInterface;
import org.squiddev.plethora.neural.NeuralContainer;

public class GuiHandler implements IGuiHandler {
	public static final int GUI_NEURAL = 101;

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				NeuralContainer container = NeuralContainer.getContainer(player);
				return container == null ? null : new GuiNeuralInterface(container);
			}
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				return NeuralContainer.getContainer(player);
			}
		}

		return null;
	}
}
