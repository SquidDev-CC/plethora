package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.*;

public final class MethodsLaser {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.LASER_S,
		target = IWorldLocation.class,
		doc = "function(yaw:number, pitch:number, potency:number) -- Fire a laser in a set direction"
	)
	@Nonnull
	public static MethodResult fire(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull Object[] args) throws LuaException {
		final double yaw = getNumber(args, 0);
		double pitchArg = getNumber(args, 1) % 360;
		final float potency = (float) getNumber(args, 2);

		// Normalise the pitch to be between -180 and 180.
		if (pitchArg > 180) pitchArg -= 360;
		final double pitch = pitchArg;

		ArgumentHelper.assertBetween(potency, minimumPotency, maximumPotency, "Potency out of range (%s).");

		CostHelpers.checkCost(unbaked.getCostHandler(), potency * cost);

		final double motionX = -Math.sin(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionZ = Math.cos(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionY = -Math.sin(pitch / 180.0f * (float) Math.PI);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> context = unbaked.bake();
				IWorldLocation location = context.getContext(IWorldLocation.class);
				Vec3d pos = location.getLoc();

				EntityLaser laser = new EntityLaser(location.getWorld(), pos);
				if (context.hasContext(TileEntity.class) || context.hasContext(ITurtleAccess.class)) {
					double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
					double hOff = 0.9; // The laser is 0.25 wide, the offset from the centre is 0.5.
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
						xOffset = motionX / length * hOff;
						yOffset = 0;
						zOffset = motionZ / length * hOff;
					}

					laser.setPosition(
						pos.xCoord + xOffset,
						pos.yCoord + yOffset,
						pos.zCoord + zOffset
					);
				} else if (context.hasContext(Entity.class)) {
					Entity entity = context.getContext(Entity.class);
					Vec3d vector = entity.getPositionVector();
					double offset = entity.width + 0.2;
					double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
					laser.setShooter(entity);

					// Offset positions to be around the edge of the entity. Avoids damaging the entity.
					laser.setPosition(
						vector.xCoord + motionX / length * offset,
						vector.yCoord + entity.getEyeHeight() + motionY / length * offset,
						vector.zCoord + motionZ / length * offset
					);
				} else {
					laser.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
				}

				laser.setPotency(potency);
				laser.setThrowableHeading(motionX, motionY, motionZ, 1.5f, 0);

				location.getWorld().spawnEntity(laser);

				return MethodResult.empty();
			}
		});
	}
}
