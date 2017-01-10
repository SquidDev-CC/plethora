package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.utils.Helpers;

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
					GuiHandler.openNeuralPlayer(player, world);
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
				GuiHandler.openNeuralEntity(player, player.worldObj, entity);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Call the right click event earlier on.
	 *
	 * @param event The event to cancel
	 */
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event) {
		if (!event.isCanceled() && Helpers.onEntityInteract(this, event.entityPlayer, event.target)) {
			event.setCanceled(true);
		}
	}

	/**
	 * Cancel the right click air event on the client side and we've hit an entity.
	 * Yes, this is horrible.
	 *
	 * @param event The event to cancel
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack current = event.entityPlayer.getHeldItem();
		if (current == null || current.getItem() != this) return;

		if (!event.isCanceled() && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.entityPlayer.worldObj.isRemote) {
			MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
			Entity entity = hit.entityHit;
			if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY &&
				!(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase
				) {
				ItemStack armor = NeuralHelpers.getStack((EntityLivingBase) entity);
				if (armor != null) event.setCanceled(true);
			}
		}
	}

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
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
