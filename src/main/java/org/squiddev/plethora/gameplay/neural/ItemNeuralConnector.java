package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		// Check if the entity we've just hit has a stack in the first slot. If so, use that instead.
		RayTraceResult hit = PlayerHelpers.findHitGuess(player);
		Entity entity = hit.entityHit;
		if (hit.typeOfHit == RayTraceResult.Type.ENTITY && !(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase) {
			ItemStack armor = NeuralHelpers.getStack((EntityLivingBase) entity);
			if (!armor.isEmpty()) return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
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
	public boolean itemInteractionForEntity(@Nonnull ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
		if (entity instanceof EntityPlayer) return false;

		ItemStack armor = NeuralHelpers.getStack(entity);
		if (!armor.isEmpty()) {
			if (!player.getEntityWorld().isRemote) {
				GuiHandler.openNeuralEntity(player, player.getEntityWorld(), entity);
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
}
