package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.PlayerHelpers;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;

public class ItemNeuralConnector extends ItemBase {
	public ItemNeuralConnector() {
		super("neuralConnector", 1);
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		// Check if the entity we've just hit has a stack in the first slot. If so, use that instead.
		RayTraceResult hit = PlayerHelpers.findHitGuess(player);
		Entity entity = hit.entityHit;
		if (hit.typeOfHit == RayTraceResult.Type.ENTITY && !(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase) {
			ItemStack armor = NeuralHelpers.getStack((EntityLivingBase) entity);
			if (armor != null) return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
		}

		if (!world.isRemote) {
			TinySlot neuralStack = NeuralHelpers.getSlot(player);
			if (neuralStack != null) {
				ServerComputer computer = ItemComputerHandler.getServer(neuralStack.getStack(), player, neuralStack);
				if (computer != null) {
					computer.turnOn();
					GuiHandler.openNeuralPlayer(player, world);
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
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (!event.isCanceled() && Helpers.onEntityInteract(this, event.getEntityPlayer(), event.getTarget(), event.getHand())) {
			event.setCanceled(true);
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
			'R', Items.REDSTONE,
			'E', Items.ENDER_PEARL,
			'I', Items.IRON_INGOT
		);
	}
}
