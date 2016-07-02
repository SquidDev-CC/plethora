package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.modules.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.ArgumentHelper.getNumber;

public final class MethodsLaser {
	@Method(IModule.class)
	public static final class MethodFire extends TargetedModuleMethod<IWorldLocation> {
		public MethodFire() {
			super("fire", true, PlethoraModules.LASER, IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			double yaw = getNumber(args, 0);
			double pitch = getNumber(args, 1);
			float potency = (float) getNumber(args, 2);

			if (potency < 0 || potency > ItemModule.LASER_MAX_DAMAGE) throw new LuaException("Potency out of range");

			double motionX = -Math.sin(yaw) * Math.cos(pitch);
			double motionZ = Math.cos(yaw) * Math.cos(pitch);
			double motionY = -Math.sin(pitch);

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
				Vec3 vector = entity.getPositionEyes(1.0f);
				double offset = entity.width + 0.2;
				double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
				laser.setPosition(
					vector.xCoord + motionX / length * offset,
					vector.yCoord + motionY / length * offset,
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

			return null;
		}
	}
}
