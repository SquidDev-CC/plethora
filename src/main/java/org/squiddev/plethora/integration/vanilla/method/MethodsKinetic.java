package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.DisableAI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic;

public final class MethodsKinetic {
	@Nonnull
	@TargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = EntityLivingBase.class,
		doc = "function(yaw:number, pitch:number, power:number) -- Launch the entity in a set direction"
	)
	public static MethodResult launch(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final float yaw = (float) getNumber(args, 0);
		final float pitch = (float) getNumber(args, 1);
		final float power = (float) getNumber(args, 2);

		assertBetween(power, 0, Kinetic.launchMax, "Power out of range (%s).");

		CostHelpers.checkCost(
			context.getCostHandler(),
			power * Kinetic.launchCost
		);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				EntityLivingBase entity = context.bake().getContext(EntityLivingBase.class);
				ItemModule.launch(entity, yaw, pitch, power);
				return MethodResult.empty();
			}
		});
	}

	@IMethod.Inject(IModuleContainer.class)
	public static final class MethodEntityLivingDisableAI extends TargetedModuleObjectMethod<EntityLiving> {
		public MethodEntityLivingDisableAI() {
			super("disableAI", PlethoraModules.KINETIC, EntityLiving.class, true, "function() -- Disable the AI of this entity");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving entity, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
			DisableAI.IDisableAIHandler disable = entity.getCapability(DisableAI.DISABLE_AI_CAPABILITY, null);
			if (disable == null) throw new LuaException("Cannot disable AI");

			disable.setDisabled(true);
			DisableAI.maybeClear(entity);

			return null;
		}
	}

	@TargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = EntityLiving.class,
		doc = "function(x:number, y:number, z:number):boolean, string|nil -- Walk to a coordinate"
	)
	@Nonnull
	public static MethodResult walk(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final double x = getNumber(args, 0);
		final double y = getNumber(args, 1);
		final double z = getNumber(args, 2);

		final double speed = optNumber(args, 3, 1);

		assertBetween(x, -Kinetic.walkRange, Kinetic.walkRange, "X coordinate out of bounds (%s)");
		assertBetween(y, -Kinetic.walkRange, Kinetic.walkRange, "Y coordinate out of bounds (%s)");
		assertBetween(z, -Kinetic.walkRange, Kinetic.walkRange, "Z coordinate out of bounds (%s)");

		if (!Double.isNaN(speed)) {
			assertBetween(speed, 1, Kinetic.walkSpeed, "Speed coordinate out of bounds (%s)");
		}

		CostHelpers.checkCost(
			context.getCostHandler(),
			Math.sqrt(x * x + y * y + z * z) * Kinetic.walkCost
		);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				EntityLiving living = context.bake().getContext(EntityLiving.class);
				PathNavigate navigator = living.getNavigator();

				Path path = navigator.getPathToXYZ(
					x + living.posX,
					y + living.posY,
					z + living.posZ
				);

				if (path == null || path.getCurrentPathLength() == 0) {
					return MethodResult.failure("No path exists");
				}

				if (!context.getCostHandler().consume(path.getCurrentPathLength() * 5 * speed)) {
					return MethodResult.failure("Insufficient energy");
				}

				return MethodResult.result(living.getNavigator().setPath(path, speed));
			}
		});
	}

	@IMethod.Inject(IModuleContainer.class)
	public static final class MethodEntityIsWalking extends TargetedModuleObjectMethod<EntityLiving> {
		public MethodEntityIsWalking() {
			super("isWalking", PlethoraModules.KINETIC, EntityLiving.class, true, "function():boolean -- Whether the entity is currently walking somewhere");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
			return new Object[]{!target.getNavigator().noPath()};
		}
	}
}
