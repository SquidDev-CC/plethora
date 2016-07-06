package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;

import static org.squiddev.plethora.gameplay.GuiHandler.GUI_FLAG_ENTITY;
import static org.squiddev.plethora.gameplay.GuiHandler.GUI_FLAG_PLAYER;

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
		if (entity instanceof EntityPlayer) return false;

		ItemStack armor = NeuralHelpers.getStack(entity);
		if (armor != null) {
			if (!player.worldObj.isRemote) {
				player.openGui(Plethora.instance, GuiHandler.GUI_NEURAL, player.worldObj, GUI_FLAG_ENTITY, entity.getEntityId(), 0);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addShapedRecipe(new ItemStack(this),
			"  R",
			"IIR",
			"IEI",
			'R', new ItemStack(Items.redstone),
			'E', new ItemStack(Items.ender_pearl),
			'I', new ItemStack(Items.iron_ingot)
		);
	}
}
