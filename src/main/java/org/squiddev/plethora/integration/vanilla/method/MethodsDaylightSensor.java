package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

/**
 * Various methods for interacting with the daylight sensor module.
 * Providers information about light levels in the area.
 */
public final class MethodsDaylightSensor {
	private MethodsDaylightSensor() {
	}

	@PlethoraMethod(module = IntegrationVanilla.daylightSensor, worldThread = false, doc = "-- Whether this world has a sky.")
	public static boolean hasSky(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		return location.getWorld().provider.hasSkyLight();
	}

	@PlethoraMethod(module = IntegrationVanilla.daylightSensor, doc = "-- The light level from the sun.")
	public static int getSkyLight(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) throws LuaException {
		World world = location.getWorld();
		if (!world.provider.hasSkyLight()) {
			throw new LuaException("The world has no sky");
		} else {
			BlockPos pos = location.getPos();
			return world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted();
		}
	}

	@PlethoraMethod(module = IntegrationVanilla.daylightSensor, doc = "-- The light level from surrounding blocks.")
	public static int getBlockLight(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		return location.getWorld().getLightFor(EnumSkyBlock.BLOCK, location.getPos());
	}

	@PlethoraMethod(module = IntegrationVanilla.daylightSensor, doc = "-- The weather in the current world.")
	public static String getWeather(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		World world = location.getWorld();
		if (world.isRaining()) {
			return world.isThundering() ? "thunder" : "rain";
		} else {
			return "clear";
		}
	}
}
