package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.*;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.ArgumentHelper.getNumber;

public final class MethodsLaser {
	@Method(IModule.class)
	public static final class MethodFire extends TargetedModuleMethod<IWorldLocation> {
		public MethodFire() {
			super("fire", PlethoraModules.LASER, IWorldLocation.class);
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> unbaked, @Nonnull Object[] args) throws LuaException {
			final double yaw = getNumber(args, 0);
			final double pitch = getNumber(args, 1);
			final float potency = (float) getNumber(args, 2);

			if (potency < 0 || potency > ItemModule.LASER_MAX_DAMAGE) throw new LuaException("Potency out of range");

			final double motionX = -Math.sin(yaw) * Math.cos(pitch);
			final double motionZ = Math.cos(yaw) * Math.cos(pitch);
			final double motionY = -Math.sin(pitch);

			// TODO: Sleep after executing

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
