package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.utils.Helpers;
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

		TinySlot neuralStack = NeuralHelpers.getSlot(player);
		if (neuralStack == null) return ActionResult.newResult(EnumActionResult.FAIL, stack);

		if (!world.isRemote) {
			ServerComputer computer = ItemComputerHandler.getServer(neuralStack.getStack(), player, neuralStack);
			if (computer != null) computer.turnOn();

			// We prevent the neural connector from opening when they're already using an interface. This
			// prevents the GUI becoming unusable when one gets in a right-click loop due to a broken program.
			if (!(player.openContainer instanceof ContainerNeuralInterface)) {
				GuiHandler.openNeuralPlayer(player, world);
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean itemInteractionForEntity(@Nonnull ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
		if (entity instanceof EntityPlayer) return false;

		TinySlot neuralStack = NeuralHelpers.getSlot(entity);
		if (neuralStack == null) return false;

		if (!player.getEntityWorld().isRemote) {
			ServerComputer computer = ItemComputerHandler.getServer(neuralStack.getStack(), player, neuralStack);
			if (computer != null) computer.turnOn();

			if (!(player.openContainer instanceof ContainerNeuralInterface)) {
				GuiHandler.openNeuralEntity(player, player.getEntityWorld(), entity);
			}
		}
		return true;
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
