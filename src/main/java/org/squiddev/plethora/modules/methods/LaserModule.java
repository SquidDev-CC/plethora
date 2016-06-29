package org.squiddev.plethora.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.modules.BlockManipulator;
import org.squiddev.plethora.modules.EntityLaser;
import org.squiddev.plethora.modules.ItemModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.ArgumentHelper.getNumber;

public class LaserModule {
	private static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.LASER);

	@Method(IModule.class)
	public static final class FireMethod extends ModuleMethod {
		public FireMethod() {
			super("fire", true, MODULE);
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			float yaw = (float) getNumber(args, 0);
			float pitch = (float) getNumber(args, 1);
			float potency = (float) getNumber(args, 2);

			if (potency < 0 || potency > ItemModule.LASER_MAX_DAMAGE) throw new LuaException("Potency out of range");

			double motionX = (double) (-MathHelper.sin(yaw) * MathHelper.cos(pitch));
			double motionZ = (double) (MathHelper.cos(yaw) * MathHelper.cos(pitch));
			double motionY = (double) (-MathHelper.sin(pitch));

			IWorldLocation location = context.getContext(IWorldLocation.class);
			BlockPos pos = location.getPos();

			EntityLaser laser = new EntityLaser(location.getWorld());
			laser.setPosition(
				pos.getX() + 0.5,
				motionY < 0 ? pos.getY() - 0.3 : pos.getY() + BlockManipulator.OFFSET + 0.1,
				pos.getZ() + 0.5
			);

			laser.setPotency(potency);
			laser.setThrowableHeading(motionX, motionY, motionZ, 1.5f, 0);

			location.getWorld().spawnEntityInWorld(laser);

			return null;
		}
	}
}
