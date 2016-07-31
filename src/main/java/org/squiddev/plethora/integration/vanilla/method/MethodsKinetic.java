package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;
import static org.squiddev.plethora.api.method.ArgumentHelper.optBoolean;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Modules.kineticLaunchMax;

public final class MethodsKinetic {
	@Method(IModule.class)
	public static final class MethodEntityLaunch extends TargetedModuleMethod<EntityLivingBase> {
		public MethodEntityLaunch() {
			super("launch", PlethoraModules.KINETIC, EntityLivingBase.class, "function(yaw:number, pitch:number, power:number) -- Launch the entity in a set direction");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final float yaw = (float) getNumber(args, 0);
			final float pitch = (float) getNumber(args, 1);
			final float power = (float) getNumber(args, 2);

			ArgumentHelper.assertBetween(power, 0, kineticLaunchMax, "Power out of range (%s).");

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					EntityLivingBase entity = context.bake().getContext(EntityLivingBase.class);
					launch(entity, yaw, pitch, power);
					return MethodResult.empty();
				}
			});
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityLivingDisableAI extends TargetedModuleObjectMethod<EntityLiving> {
		public MethodEntityLivingDisableAI() {
			super("disableAI", PlethoraModules.KINETIC, EntityLiving.class, true, "function([disable:boolean]) -- Disable the AI of this entity");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving entity, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			entity.setNoAI(optBoolean(args, 0, true));
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
