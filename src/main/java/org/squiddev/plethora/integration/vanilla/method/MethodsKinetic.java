package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.DisableAI;

import javax.annotation.Nonnull;

import static dan200.computercraft.api.lua.ArgumentHelper.getFiniteDouble;
import static dan200.computercraft.api.lua.ArgumentHelper.optFiniteDouble;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic;
import static org.squiddev.plethora.utils.Helpers.normaliseAngle;

@Injects
public final class MethodsKinetic {
	public static final SubtargetedModuleMethod<Entity> LAUNCH = SubtargetedModuleMethod.of(
		"launch", PlethoraModules.KINETIC_M, Entity.class,
		"function(yaw:number, pitch:number, power:number) -- Launch the entity in a set direction",
		MethodsKinetic::launch
	);

	private MethodsKinetic() {
	}

	private static MethodResult launch(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final float yaw = (float) normaliseAngle(getFiniteDouble(args, 0));
		final float pitch = (float) normaliseAngle(getFiniteDouble(args, 1));
		final float power = (float) getFiniteDouble(args, 2);

		assertBetween(power, 0, Kinetic.launchMax, "Power out of range (%s).");

		return context.getCostHandler().await(power * Kinetic.launchCost, MethodResult.nextTick(() -> {
			Entity entity = context.bake().getContext(ContextKeys.ORIGIN, Entity.class);
			ItemModule.launch(entity, yaw, pitch, power);
			return MethodResult.empty();
		}));
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "-- Disable the AI of this entity. Their neural pathways will be inhibited preventing them thinking for themselves"
	)
	public static void disableAI(@FromSubtarget(ContextKeys.ORIGIN) EntityLiving entity) throws LuaException {
		DisableAI.IDisableAIHandler disable = entity.getCapability(DisableAI.DISABLE_AI_CAPABILITY, null);
		if (disable == null) throw new LuaException("Cannot disable AI");

		disable.setDisabled(true);
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "-- Enable the AI of this entity."
	)
	public static void enableAI(@FromSubtarget(ContextKeys.ORIGIN) EntityLiving entity) throws LuaException {
		DisableAI.IDisableAIHandler disable = entity.getCapability(DisableAI.DISABLE_AI_CAPABILITY, null);
		if (disable == null) throw new LuaException("Cannot enable AI");

		disable.setDisabled(false);
	}

	public static final SubtargetedModuleMethod<EntityLiving> WALK = SubtargetedModuleMethod.of(
		"walk", PlethoraModules.KINETIC_M, EntityLiving.class,
		"function(x:number, y:number, z:number):boolean, string|nil -- Walk to a coordinate",
		MethodsKinetic::walk
	);

	@Nonnull
	private static MethodResult walk(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final double x = getFiniteDouble(args, 0);
		final double y = getFiniteDouble(args, 1);
		final double z = getFiniteDouble(args, 2);

		final double speed = optFiniteDouble(args, 3, 1);

		assertBetween(x, -Kinetic.walkRange, Kinetic.walkRange, "X coordinate out of bounds (%s)");
		assertBetween(y, -Kinetic.walkRange, Kinetic.walkRange, "Y coordinate out of bounds (%s)");
		assertBetween(z, -Kinetic.walkRange, Kinetic.walkRange, "Z coordinate out of bounds (%s)");

		if (!Double.isNaN(speed)) {
			assertBetween(speed, 1, Kinetic.walkSpeed, "Speed coordinate out of bounds (%s)");
		}

		double cost = Math.sqrt(x * x + y * y + z * z) * Kinetic.walkCost;
		return context.getCostHandler().await(cost, MethodResult.nextTick(() -> {
			EntityLiving living = context.bake().getContext(ContextKeys.ORIGIN, EntityLiving.class);
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
		}));
	}

	@PlethoraMethod(module = PlethoraModules.KINETIC_S, doc = "-- Whether the entity is currently walking somewhere.")
	public static boolean isWalking(@FromSubtarget(ContextKeys.ORIGIN) EntityLiving target) {
		return !target.getNavigator().noPath();
	}
}
