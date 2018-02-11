package org.squiddev.plethora.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IPlayerOwnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PlayerHelpers {
	private static final Predicate<Entity> collidablePredicate = Predicates.and(
		EntitySelectors.NOT_SPECTATING,
		new Predicate<Entity>() {
			@Override
			public boolean apply(Entity entity) {
				return entity.canBeCollidedWith();
			}
		}
	);

	@Nonnull
	public static RayTraceResult findHitGuess(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			return findHit((EntityPlayerMP) player);
		} else if (player.worldObj.isRemote && player instanceof EntityPlayerSP) {
			RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
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
			origin.xCoord + look.xCoord * range,
			origin.yCoord + look.yCoord * range,
			origin.zCoord + look.zCoord * range
		);

		RayTraceResult hit = entity.worldObj.rayTraceBlocks(origin, target);

		List<Entity> entityList = entity.worldObj.getEntitiesInAABBexcluding(
			entity,
			entity.getEntityBoundingBox().addCoord(
				look.xCoord * range,
				look.yCoord * range,
				look.zCoord * range
			).expand(1, 1, 1), collidablePredicate);

		Entity closestEntity = null;
		Vec3d closestVec = null;
		double closestDistance = range;
		for (Entity entityHit : entityList) {
			float size = entityHit.getCollisionBorderSize();
			AxisAlignedBB box = entityHit.getEntityBoundingBox().expand((double) size, (double) size, (double) size);
			RayTraceResult intercept = box.calculateIntercept(origin, target);

			if (box.isVecInside(origin)) {
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

	@Nullable
	public static GameProfile getProfile(Entity entity) {
		if (entity instanceof EntityPlayer) {
			return ((EntityPlayer) entity).getGameProfile();
		} else if (entity instanceof IPlayerOwnable) {
			return ((IPlayerOwnable) entity).getOwningProfile();
		} else {
			return null;
		}
	}

	@Nullable
	public static GameProfile readProfile(@Nonnull NBTTagCompound tag) {
		if (!tag.hasKey("owner", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND)) {
			return null;
		}

		NBTTagCompound owner = tag.getCompoundTag("owner");
		return new GameProfile(
			new UUID(owner.getLong("upper_id"), owner.getLong("lower_id")),
			owner.getString("name")
		);
	}

	public static void writeProfile(@Nonnull NBTTagCompound tag, @Nullable GameProfile profile) {
		if (profile == null) {
			tag.removeTag("owner");
		} else {
			NBTTagCompound owner = new NBTTagCompound();
			tag.setTag("owner", owner);

			owner.setLong("upper_id", profile.getId().getMostSignificantBits());
			owner.setLong("lower_id", profile.getId().getLeastSignificantBits());
			owner.setString("name", profile.getName());
		}
	}
}
