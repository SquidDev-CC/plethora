package org.squiddev.plethora.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerHelpers {
	private static final Predicate<Entity> collidablePredicate = Predicates.and(
		EntitySelectors.NOT_SPECTATING,
		new Predicate<Entity>() {
			public boolean apply(Entity entity) {
				return entity.canBeCollidedWith();
			}
		}
	);

	@Nonnull
	public static RayTraceResult findHitGuess(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			return findHit((EntityPlayerMP) player);
		} else if (player.getEntityWorld().isRemote && player instanceof EntityPlayerSP) {
			RayTraceResult result =  Minecraft.getMinecraft().objectMouseOver;
			return result == null ? new RayTraceResult(RayTraceResult.Type.MISS, player.getPositionVector(), null, null) : result;
		} else {
			return findHit(player, 4.5);
		}
	}

	@Nonnull
	public static RayTraceResult findHit(EntityPlayerMP player) {
		return findHit(player, player.interactionManager.getBlockReachDistance());
	}

	@Nonnull
	public static RayTraceResult findHit(EntityPlayerMP player, EntityLivingBase entity) {
		return findHit(entity, player.interactionManager.getBlockReachDistance());
	}

	@Nonnull
	public static RayTraceResult findHit(EntityLivingBase entity, double range) {
		Vec3d origin = new Vec3d(
			entity.posX,
			entity.posY + entity.getEyeHeight(),
			entity.posZ
		);

		Vec3d look = entity.getLookVec();
		Vec3d target = new Vec3d(
			origin.x + look.x * range,
			origin.y + look.y * range,
			origin.z + look.z * range
		);

		RayTraceResult hit = entity.getEntityWorld().rayTraceBlocks(origin, target);

		List<Entity> entityList = entity.getEntityWorld().getEntitiesInAABBexcluding(
			entity,
			entity.getEntityBoundingBox().offset(
				look.x * range,
				look.y * range,
				look.z * range
			).expand(1, 1, 1), collidablePredicate);

		Entity closestEntity = null;
		Vec3d closestVec = null;
		double closestDistance = range;
		for (Entity entityHit : entityList) {
			float size = entityHit.getCollisionBorderSize();
			AxisAlignedBB box = entityHit.getEntityBoundingBox().expand((double) size, (double) size, (double) size);
			RayTraceResult intercept = box.calculateIntercept(origin, target);

			if (box.contains(origin)) {
				if (closestDistance >= 0.0D) {
					closestEntity = entityHit;
					closestVec = intercept == null ? origin : intercept.hitVec;
					closestDistance = 0.0D;
				}
			} else if (intercept != null) {
				double distance = origin.distanceTo(intercept.hitVec);

				if (distance < closestDistance || closestDistance == 0.0D) {
					if (entityHit == entityHit.getRidingEntity() && !entityHit.canRiderInteract()) {
						if (closestDistance == 0.0D) {
							closestEntity = entityHit;
							closestVec = intercept.hitVec;
						}
					} else {
						closestEntity = entityHit;
						closestVec = intercept.hitVec;
						closestDistance = distance;
					}
				}
			}
		}

		if (closestEntity instanceof EntityLivingBase && closestDistance <= range && (hit == null || entity.getDistanceSq(hit.getBlockPos()) > closestDistance * closestDistance)) {
			return new RayTraceResult(closestEntity, closestVec);
		} else if (hit == null) {
			return new RayTraceResult(RayTraceResult.Type.MISS, origin, null, null);
		} else {
			return hit;
		}
	}
}
