package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import java.util.concurrent.Callable;

/**
 * Various methods for interacting with the clock module
 */
public class MethodsClock {
	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class,
		doc = "function():integer -- The game time in ticks"
	)
	public static MethodResult getTime(final IUnbakedContext<IModuleContainer> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				World world = context.bake().getContext(IWorldLocation.class).getWorld();
				return MethodResult.result(world.getWorldTime() % 24000);
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class,
		doc = "function():integer -- The current day of this world"
	)
	public static MethodResult getDay(final IUnbakedContext<IModuleContainer> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				World world = context.bake().getContext(IWorldLocation.class).getWorld();
				return MethodResult.result(world.getWorldTime() / 240000);
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class,
		doc = "function():number -- The angle the sun or moon lies at in degrees. 0 is directly overhead."
	)
	public static MethodResult getCelestialAngle(final IUnbakedContext<IModuleContainer> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				World world = context.bake().getContext(IWorldLocation.class).getWorld();
				return MethodResult.result(world.getCelestialAngle(1) * 360);
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class,
		doc = "function():number -- The current phase of the moon"
	)
	public static MethodResult getMoonPhase(final IUnbakedContext<IModuleContainer> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				World world = context.bake().getContext(IWorldLocation.class).getWorld();

				// World#getMoonPhase() is client only :(
				return MethodResult.result(world.provider.getMoonPhase(world.getWorldTime()));
			}
		});
	}
}
