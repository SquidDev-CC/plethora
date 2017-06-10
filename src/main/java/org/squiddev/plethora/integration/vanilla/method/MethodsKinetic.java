package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
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
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = Entity.class,
		doc = "function(yaw:number, pitch:number, power:number) -- Launch the entity in a set direction"
	)
	public static MethodResult launch(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final float yaw = (float) getReal(args, 0) % 360;
		final float pitch = (float) getReal(args, 1) % 360;
		final float power = (float) getReal(args, 2);

		assertBetween(power, 0, Kinetic.launchMax, "Power out of range (%s).");

		CostHelpers.checkCost(
			context.getCostHandler(),
			power * Kinetic.launchCost
		);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				Entity entity = context.bake().getContext(Entity.class);
				ItemModule.launch(entity, yaw, pitch, power);
				return MethodResult.empty();
			}
		});
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLiving.class, worldThread = true,
		doc = "function() -- Disable the AI of this entity. Be warned: this permanently scars them - they'll never be the same again!"
	)
	@Nullable
	public static Object[] disableAI(@Nonnull EntityLiving entity, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		DisableAI.IDisableAIHandler disable = entity.getCapability(DisableAI.DISABLE_AI_CAPABILITY, null);
		if (disable == null) throw new LuaException("Cannot disable AI");

		disable.setDisabled(true);
		DisableAI.maybeClear(entity);

		return null;
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = EntityLiving.class,
		doc = "function(x:number, y:number, z:number):boolean, string|nil -- Walk to a coordinate"
	)
	@Nonnull
	public static MethodResult walk(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final double x = getReal(args, 0);
		final double y = getReal(args, 1);
		final double z = getReal(args, 2);

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

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLiving.class, worldThread = true,
		doc = "function():boolean -- Whether the entity is currently walking somewhere"
	)
	@Nullable
	public static Object[] isWalking(@Nonnull EntityLiving target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		return new Object[]{!target.getNavigator().noPath()};
	}
}
