package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.GuiHandler.GUI_FLAG_ENTITY;
import static org.squiddev.plethora.gameplay.GuiHandler.GUI_FLAG_PLAYER;

public class ItemNeuralConnector extends ItemBase {
	public ItemNeuralConnector() {
		super("neuralConnector", 1);
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
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

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
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
			'R', new ItemStack(Items.REDSTONE),
			'E', new ItemStack(Items.ENDER_PEARL),
			'I', new ItemStack(Items.IRON_INGOT)
		);
	}
}
