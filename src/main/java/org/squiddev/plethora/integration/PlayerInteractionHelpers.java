package org.squiddev.plethora.integration;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerInteractionHelpers {
	//region Use
	@Nonnull
	public static MethodResult use(@Nonnull EntityPlayerMP player, @Nonnull RayTraceResult hit, @Nonnull EnumHand hand, int duration) {
		ItemStack stack = player.getHeldItem(hand);
		World world = player.getEntityWorld();

		switch (hit.typeOfHit) {
			case ENTITY:
				if (stack != null) {
					EnumActionResult result = player.interact(hit.entityHit, stack, hand);
					if (result != EnumActionResult.PASS) {
						return MethodResult.result(result == EnumActionResult.SUCCESS, "entity", "interact");
					}
				}
				break;
			case BLOCK: {
				// When right next to a block the hit direction gets inverted. Try both to see if one works.
				Object[] result = tryUseOnBlock(player, world, hit, stack, hand, hit.sideHit);
				if (result != null) return MethodResult.result(result);

				result = tryUseOnBlock(player, world, hit, stack, hand, hit.sideHit.getOpposite());
				if (result != null) return MethodResult.result(result);
			}
		}

		if (stack != null) {
			if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickEmpty(player, hand))) {
				return MethodResult.result(true, "item", "use");
			}

			duration = Math.min(duration, stack.getMaxItemUseDuration());
			ActionResult<ItemStack> result = stack.useItemRightClick(player.getEntityWorld(), player, hand);

			ItemStack resultStack = result.getResult();
			player.setHeldItem(hand, resultStack);
			if (resultStack == null || resultStack.stackSize == 0) {
				player.setHeldItem(hand, null);
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
			}

			switch (result.getType()) {
				case FAIL:
					return MethodResult.delayedResult(duration, false, "item", "use");
				case SUCCESS:
					ItemStack active = player.getActiveItemStack();
					if (active!= null && !ForgeEventFactory.onUseItemStop(player, active, duration)) {
						active.onPlayerStoppedUsing(player.getEntityWorld(), player, active.getMaxItemUseDuration() - duration);
						player.resetActiveHand();
						return MethodResult.delayedResult(duration, true, "item", "use");
					} else {
						return MethodResult.result(true, "item", "use");
					}
			}
		}

		return MethodResult.failure("Nothing to do here");
	}

	private static Object[] tryUseOnBlock(EntityPlayer player, World world, RayTraceResult hit, ItemStack stack, EnumHand hand, EnumFacing side) {
		IBlockState state = world.getBlockState(hit.getBlockPos());
		if (!state.getBlock().isAir(state, world, hit.getBlockPos())) {
			if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, hand, stack, hit.getBlockPos(), side, hit.hitVec))) {
				return new Object[]{true, "block", "interact"};
			}

			Object[] result = onPlayerRightClick(player, stack, hand, hit.getBlockPos(), side, hit.hitVec);
			if (result != null) return result;
		}

		return null;
	}

	private static Object[] onPlayerRightClick(EntityPlayer player, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing side, Vec3d look) {
		float xCoord = (float) look.xCoord - pos.getX();
		float yCoord = (float) look.yCoord - pos.getY();
		float zCoord = (float) look.zCoord - pos.getZ();
		World world = player.getEntityWorld();

		if (stack != null && stack.getItem().onItemUseFirst(stack, player, world, pos, side, xCoord, yCoord, zCoord, hand) == EnumActionResult.SUCCESS) {
			return new Object[]{true, "item", "use"};
		}

		if (!player.isSneaking() || stack == null || stack.getItem().doesSneakBypassUse(stack, world, pos, player)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().onBlockActivated(world, pos, state, player, hand, stack, side, xCoord, yCoord, zCoord)) {
				return new Object[]{true, "block", "interact"};
			}
		}

		if (stack == null) return null;

		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock itemBlock = (ItemBlock) stack.getItem();

			Block block = world.getBlockState(pos).getBlock();
			BlockPos shiftPos = pos;
			EnumFacing shiftSide = side;
			if (block == Blocks.SNOW_LAYER && block.isReplaceable(world, pos)) {
				shiftSide = EnumFacing.UP;
			} else if (!block.isReplaceable(world, pos)) {
				shiftPos = pos.offset(side);
			}

			if (!world.canBlockBePlaced(itemBlock.getBlock(), shiftPos, false, shiftSide, null, stack)) {
				return null;
			}
		}

		int stackMetadata = stack.getMetadata();
		int stackSize = stack.stackSize;

		boolean flag = stack.onItemUse(player, world, pos, hand, side, xCoord, yCoord, zCoord) == EnumActionResult.SUCCESS;

		if (player.capabilities.isCreativeMode) {
			stack.setItemDamage(stackMetadata);
			stack.stackSize = stackSize;
		} else if (stack.stackSize <= 0) {
			player.setHeldItem(hand, null);
			MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
		}

		if (flag) {
			return new Object[]{true, "place"};
		}

		return null;
	}
	//endregion

	public static Pair<Boolean, String> attack(@Nonnull EntityPlayerMP player, @Nullable Entity hitEntity) {
		if (hitEntity != null) {
			// TODO: Use the original entity for the main attacking
			player.attackTargetEntityWithCurrentItem(hitEntity);
			return Pair.of(true, "entity");
		}

		return Pair.of(false, "Nothing to attack here");
	}
}
