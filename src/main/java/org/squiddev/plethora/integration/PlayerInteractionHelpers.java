package org.squiddev.plethora.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerInteractionHelpers {
	//region Use

	/**
	 * Use an item
	 *
	 * @see net.minecraft.network.NetHandlerPlayServer#processUseEntity(CPacketUseEntity)
	 * @see net.minecraft.client.multiplayer.PlayerControllerMP
	 * @see Minecraft#rightClickMouse()
	 */
	@Nonnull
	public static MethodResult use(@Nonnull EntityPlayerMP player, @Nonnull RayTraceResult hit, @Nonnull EnumHand hand, int duration) {
		ItemStack stack = player.getHeldItem(hand);
		World world = player.getEntityWorld();

		switch (hit.typeOfHit) {
			case ENTITY: {
				Entity target = hit.entityHit;

				// PlayerControllerMP.interactWithEntity (with hit position)
				Vec3d vec3d = new Vec3d(hit.hitVec.x - target.posX, hit.hitVec.y - target.posY, hit.hitVec.z - target.posZ);
				EnumActionResult cancelResult = ForgeHooks.onInteractEntityAt(player, target, hit, hand);
				if (cancelResult == null) cancelResult = target.applyPlayerInteraction(player, vec3d, hand);

				if (cancelResult == EnumActionResult.SUCCESS) return MethodResult.result(true, "entity");

				// PlayerControllerMP.interactWithEntiy (without hit position)
				if (player.interactOn(target, hand) == EnumActionResult.SUCCESS) {
					return MethodResult.result(true, "entity");
				}
				break;
			}
			case BLOCK: {
				// When right next to a block the hit direction gets inverted. Try both to see if one works.
				BlockPos pos = hit.getBlockPos();
				if (!world.isAirBlock(pos) && world.getWorldBorder().contains(pos)) {
					float hitX = (float) hit.hitVec.x - (float) pos.getX();
					float hitY = (float) hit.hitVec.y - (float) pos.getY();
					float hitZ = (float) hit.hitVec.z - (float) pos.getZ();

					EnumActionResult result = player.interactionManager.processRightClickBlock(player, world, stack, hand, pos, hit.sideHit, hitX, hitY, hitZ);
					if (result == EnumActionResult.SUCCESS) return MethodResult.result(true, "block");

					result = player.interactionManager.processRightClickBlock(player, world, stack, hand, pos, hit.sideHit.getOpposite(), hitX, hitY, hitZ);
					if (result == EnumActionResult.SUCCESS) return MethodResult.result(true, "block");
				}
			}
		}

		if (stack.isEmpty() && hit.typeOfHit == RayTraceResult.Type.MISS) ForgeHooks.onEmptyClick(player, hand);
		if (!stack.isEmpty()) {
			EnumActionResult result = player.interactionManager.processRightClick(player, world, stack, hand);
			if (result == EnumActionResult.SUCCESS) {
				ItemStack active = player.getActiveItemStack();

				if (!active.isEmpty()) {
					return MethodResult.delayed(duration, () -> {
						// If we're still holding this item, it's still there and we haven't started using something else.
						if (player.getHeldItem(hand) == active && !active.isEmpty() &&
							(player.getActiveItemStack() == active || player.getActiveItemStack().isEmpty())) {
							// Then stop it!
							if (!ForgeEventFactory.onUseItemStop(player, active, duration)) {
								active.onPlayerStoppedUsing(player.getEntityWorld(), player, active.getMaxItemUseDuration() - duration);
							}

							player.resetActiveHand();
							return MethodResult.result(true, "item");
						} else {
							return MethodResult.result(false);
						}
					});
				} else {
					return MethodResult.result(true, "item");
				}
			}
		}

		return MethodResult.result(false);
	}
	//endregion

	/**
	 * Attack an entity with a player
	 *
	 * @param player    The player who is attacking
	 * @param hitEntity The entity which was attacked
	 * @return If this entity could be attacked.
	 */
	public static Pair<Boolean, String> attack(@Nonnull EntityPlayerMP player, @Nullable Entity hitEntity) {
		if (hitEntity != null) {
			// TODO: Use the original entity for the main attacker
			player.attackTargetEntityWithCurrentItem(hitEntity);
			return Pair.of(true, "entity");
		}

		return Pair.of(false, "Nothing to attack here");
	}
}
