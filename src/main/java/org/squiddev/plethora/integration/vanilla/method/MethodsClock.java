package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.gen.FromContext;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

/**
 * Various methods for interacting with the clock module
 */
public class MethodsClock {
	@PlethoraMethod(module = IntegrationVanilla.clock, worldThread = false, doc = "-- The game time in ticks")
	public static long getTime(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		return location.getWorld().getWorldTime() % 24000;
	}

	@PlethoraMethod(module = IntegrationVanilla.clock, worldThread = false,
		doc = "function():integer -- The current day of this world"
	)
	public static long getDay(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		return location.getWorld().getWorldTime() / 24000;
	}

	@PlethoraMethod(
		module = IntegrationVanilla.clock, worldThread = false,
		doc = "-- The angle the sun or moon lies at in degrees. 0 is directly overhead."
	)
	public static float getCelestialAngle(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		return location.getWorld().getCelestialAngle(1) * 360;
	}

	@PlethoraMethod(module = IntegrationVanilla.clock, worldThread = false, doc = "-- The current phase of the moon")
	public static int getMoonPhase(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		World world = location.getWorld();

		// World#getMoonPhase() is client only :(
		return world.provider.getMoonPhase(world.getWorldTime());
	}
}
