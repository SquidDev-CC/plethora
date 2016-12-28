package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.BlockManipulator;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.TileManipulator;

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
		final double pitch = getNumber(args, 1);
		final float potency = (float) getNumber(args, 2);

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
				Vec3 pos = location.getLoc();

				EntityLaser laser = new EntityLaser(location.getWorld(), pos);
				if (context.hasContext(TileManipulator.class)) {
					double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
					double y = pos.yCoord - 0.5;
					double hOff = 1.2;
					double vOff = 0.1;

					// Offset positions to be around the edge of the manipulator. Avoids breaking the manipulator and
					// the block below/above in most cases.
					// Also offset to be just above/below the manipulator, depending on the pitch.
					laser.setPosition(
						pos.xCoord + motionX / length * hOff,
						motionY < 0 ? y - vOff : y + BlockManipulator.OFFSET + vOff,
						pos.zCoord + motionZ / length * hOff
					);
				} else if (context.hasContext(EntityLivingBase.class)) {
					EntityLivingBase entity = context.getContext(EntityLivingBase.class);
					Vec3 vector = entity.getPositionVector();
					double offset = entity.width + 0.2;
					double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
					laser.setShooter(entity);

					// Offset positions to be around the edge of the entity. Avoids damaging the entity.
					laser.setPosition(
						vector.xCoord + motionX / length * offset,
						vector.yCoord + entity.getEyeHeight() + motionY / length * offset,
						vector.zCoord + motionZ / length * offset
					);
				}

				laser.setPotency(potency);
				laser.setThrowableHeading(motionX, motionY, motionZ, 1.5f, 0);

				location.getWorld().spawnEntityInWorld(laser);

				return MethodResult.empty();
			}
		});
	}
}
