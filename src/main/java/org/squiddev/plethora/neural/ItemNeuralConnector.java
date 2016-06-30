package org.squiddev.plethora.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.squiddev.plethora.GuiHandler;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;

import static org.squiddev.plethora.GuiHandler.GUI_FLAG_ENTITY;
import static org.squiddev.plethora.GuiHandler.GUI_FLAG_PLAYER;

public class ItemNeuralConnector extends ItemBase {
	public ItemNeuralConnector() {
		super("neuralConnector", 1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			ItemStack neuralStack = NeuralHelpers.getStack(player);
			if (neuralStack != null) {
				ServerComputer computer = ItemComputerHandler.getServer(neuralStack, player, player.inventory);
				if (computer != null) {
					computer.turnOn();
					player.openGui(Plethora.instance, GuiHandler.GUI_NEURAL, player.worldObj, GUI_FLAG_PLAYER, 0, 0);
				}
			}
		}

		return stack;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
		ItemStack armor = NeuralHelpers.getStack(entity);
		if (armor != null) {
			if (!player.worldObj.isRemote) {
				player.openGui(Plethora.instance, GuiHandler.GUI_NEURAL, player.worldObj, GUI_FLAG_ENTITY, entity.getEntityId(), 0);
			}
			return true;
		} else {
			return super.itemInteractionForEntity(stack, player, entity);
		}
	}
}
