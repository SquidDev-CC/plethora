package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.BlockManipulator;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.TileManipulator;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Modules.laserMaximum;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Modules.laserMinimum;

public final class MethodsLaser {
	@IMethod.Inject(IModule.class)
	public static final class MethodFire extends TargetedModuleMethod<IWorldLocation> {
		public MethodFire() {
			super("fire", PlethoraModules.LASER, IWorldLocation.class, "function(yaw:number, pitch:number, potency:number) -- Fire a laser in a set direction");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> unbaked, @Nonnull Object[] args) throws LuaException {
			final double yaw = getNumber(args, 0);
			final double pitch = getNumber(args, 1);
			final float potency = (float) getNumber(args, 2);

			ArgumentHelper.assertBetween(potency, laserMinimum, laserMaximum, "Potency out of range (%s).");

			CostHelpers.checkCost(unbaked.getCostHandler(), potency * 10);

			final double motionX = -Math.sin(yaw) * Math.cos(pitch);
			final double motionZ = Math.cos(yaw) * Math.cos(pitch);
			final double motionY = -Math.sin(pitch);

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					IContext<IModule> context = unbaked.bake();
					IWorldLocation location = context.getContext(IWorldLocation.class);
					BlockPos pos = location.getPos();

					EntityLaser laser = new EntityLaser(location.getWorld());
					if (context.hasContext(TileManipulator.class)) {
						laser.setPosition(
							pos.getX() + 0.5,
							motionY < 0 ? pos.getY() - 0.3 : pos.getY() + BlockManipulator.OFFSET + 0.1,
							pos.getZ() + 0.5
						);
					} else if (context.hasContext(EntityLivingBase.class)) {
						EntityLivingBase entity = context.getContext(EntityLivingBase.class);
						Vec3 vector = entity.getPositionVector();
						double offset = entity.width + 0.2;
						double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
						laser.setPosition(
							vector.xCoord + motionX / length * offset,
							vector.yCoord + entity.getEyeHeight() + motionY / length * offset,
							vector.zCoord + motionZ / length * offset
						);
					} else {
						laser.setPosition(
							pos.getX() + 0.5,
							pos.getY() + 0.5,
							pos.getZ() + 0.5
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
}
