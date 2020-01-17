package org.squiddev.plethora.gameplay.modules.methods;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.utils.PlayerHelpers;

import javax.annotation.Nonnull;

import static dan200.computercraft.api.lua.ArgumentHelper.getFiniteDouble;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.*;

@Injects
public final class MethodsLaser {
	public static final SubtargetedModuleMethod<IWorldLocation> FIRE = SubtargetedModuleMethod.of(
		MethodsLaser.class.getName() + "#fire",
		"fire", PlethoraModules.LASER_M, IWorldLocation.class,
		"function(yaw:number, pitch:number, potency:number) -- Fire a laser in a set direction",
		MethodsLaser::fire
	);

	private MethodsLaser() {
	}

	@Nonnull
	private static MethodResult fire(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull Object[] args) throws LuaException {
		final double yaw = getFiniteDouble(args, 0) % 360;
		double pitchArg = getFiniteDouble(args, 1) % 360;
		final float potency = (float) getFiniteDouble(args, 2);

		// Normalise the pitch to be between -180 and 180.
		if (pitchArg > 180) pitchArg -= 360;
		final double pitch = pitchArg;

		ArgumentHelper.assertBetween(potency, minimumPotency, maximumPotency, "Potency out of range (%s).");

		final double motionX = -Math.sin(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionZ = Math.cos(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionY = -Math.sin(pitch / 180.0f * (float) Math.PI);

		return unbaked.getCostHandler().await(potency * cost, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> context = unbaked.bake();
			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			Vec3d pos = location.getLoc();

			EntityLaser laser = new EntityLaser(location.getWorld(), pos);
			{
				IPlayerOwnable ownable = context.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
				Entity entity = context.getContext(ContextKeys.ORIGIN, Entity.class);

				GameProfile profile = null;
				if (ownable != null) profile = ownable.getOwningProfile();
				if (profile == null) profile = PlayerHelpers.getProfile(entity);

				laser.setShooter(entity, profile);
			}
			if (context.hasContext(TileEntity.class) || context.hasContext(ITurtleAccess.class)) {
				double vOff = 0.3; // The laser is 0.25 high, so we add a little more.

				// Offset positions to be around the edge of the manipulator. Avoids breaking the manipulator and
				// the block below/above in most cases.
				// Also offset to be just above/below the manipulator, depending on the pitch.

				double yOffset, xOffset, zOffset;
				if (pitch < -60) {
					xOffset = 0;
					yOffset = 0.5 + vOff;
					zOffset = 0;
				} else if (pitch > 60) {
					xOffset = 0;
					yOffset = -0.5 - vOff;
					zOffset = 0;
				} else {
					// The laser is 0.25 wide, the offset from the centre is 0.5.
					double hOff = 0.9;
					double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
					xOffset = motionX / length * hOff;
					yOffset = 0;
					zOffset = motionZ / length * hOff;
				}

				laser.setPosition(
					pos.x + xOffset,
					pos.y + yOffset,
					pos.z + zOffset
				);
			} else if (context.hasContext(Entity.class)) {
				Entity entity = context.getContext(Entity.class);
				Vec3d vector = entity.getPositionVector();
				double offset = entity.width + 0.2;
				double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);


				// Offset positions to be around the edge of the entity. Avoids damaging the entity.
				laser.setPosition(
					vector.x + motionX / length * offset,
					vector.y + entity.getEyeHeight() + motionY / length * offset,
					vector.z + motionZ / length * offset
				);
			} else {
				laser.setPosition(pos.x, pos.y, pos.z);
			}

			laser.setPotency(potency);
			laser.shoot(motionX, motionY, motionZ, 1.5f, 0);

			location.getWorld().spawnEntity(laser);

			return MethodResult.empty();
		}));
	}
}
