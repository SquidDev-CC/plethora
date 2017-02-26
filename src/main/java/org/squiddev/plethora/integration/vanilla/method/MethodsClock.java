package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

/**
 * Various methods for interacting with the clock module
 */
public class MethodsClock {
	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class, worldThread = false,
		doc = "function():integer -- The game time in ticks"
	)
	public static Object[] getTime(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		return new Object[]{world.getWorldTime() % 24000};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class, worldThread = false,
		doc = "function():integer -- The current day of this world"
	)
	public static Object[] getDay(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		return new Object[]{world.getWorldTime() / 240000};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class, worldThread = false,
		doc = "function():number -- The angle the sun or moon lies at in degrees. 0 is directly overhead."
	)
	public static Object[] getCelestialAngle(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		return new Object[]{world.getCelestialAngle(1) * 360};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.clock, target = IWorldLocation.class, worldThread = false,
		doc = "function():number -- The current phase of the moon"
	)
	public static Object[] getMoonPhase(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();

		// World#getMoonPhase() is client only :(
		return new Object[]{world.provider.getMoonPhase(world.getWorldTime())};
	}
}
