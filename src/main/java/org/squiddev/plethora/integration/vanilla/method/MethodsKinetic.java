package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.ArgumentHelper.getNumber;
import static org.squiddev.plethora.modules.ItemModule.KINETIC_LAUNCH_MAX;

public final class MethodsKinetic {
	@Method(IModule.class)
	public static final class MethodEntityLaunch extends TargetedModuleMethod<EntityLivingBase> {
		public MethodEntityLaunch() {
			super("launch", true, PlethoraModules.KINETIC, EntityLivingBase.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLivingBase entity, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			float yaw = (float) getNumber(args, 0);
			float pitch = (float) getNumber(args, 1);
			float power = (float) getNumber(args, 2);

			if (power < 0 || power > KINETIC_LAUNCH_MAX) throw new LuaException("Power out of range");

			if (entity.isAirBorne && !(entity instanceof EntityFlying)) throw new LuaException("Entity is in the air");

			launch(entity, yaw, pitch, power);

			return null;
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityLivingDisableAI extends TargetedModuleMethod<EntityLiving> {
		public MethodEntityLivingDisableAI() {
			super("disableAI", true, PlethoraModules.KINETIC, EntityLiving.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving entity, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			entity.setNoAI(true);
			return null;
		}
	}

	public static void launch(EntityLivingBase entity, float yaw, float pitch, float power) {
		float motionX = -MathHelper.sin(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionZ = MathHelper.cos(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionY = -MathHelper.sin(pitch / 180.0f * (float) Math.PI);

		power /= MathHelper.sqrt_float(motionX * motionX + motionY * motionY + motionZ * motionZ);
		entity.addVelocity(motionX * power, motionY * power, motionZ * power);
		entity.velocityChanged = true;
	}
}
