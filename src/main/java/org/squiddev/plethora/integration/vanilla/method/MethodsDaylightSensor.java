package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

/**
 * Various methods for interacting with the daylight sensor module.
 * Providers information about light levels in the area.
 */
public class MethodsDaylightSensor {
	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class, worldThread = false,
		doc = "function():boolean -- Whether this world has a sky"
	)
	public static Object[] hasSky(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		return new Object[]{world.provider.hasSkyLight()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class, worldThread = true,
		doc = "function():int -- The light level from the sun"
	)
	public static Object[] getSkyLight(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) throws LuaException {
		World world = location.getWorld();
		if (!world.provider.hasSkyLight()) {
			throw new LuaException("The world has no sky");
		} else {
			BlockPos pos = location.getPos();
			return new Object[]{world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted()};
		}
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class, worldThread = true,
		doc = "function():int -- The light level from surrounding blocks"
	)
	public static Object[] getBlockLight(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		BlockPos pos = location.getPos();
		return new Object[]{world.getLightFor(EnumSkyBlock.BLOCK, pos)};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class, worldThread = false,
		doc = "function():string -- The weather in the current world"
	)
	public static Object[] getWeather(IWorldLocation location, IContext<IModuleContainer> context, Object[] args) {
		World world = location.getWorld();
		if (world.isRaining()) {
			if (world.isThundering()) {
				return new Object[]{"thunder"};
			} else {
				return new Object[]{"rain"};
			}
		} else {
			return new Object[]{"clear"};
		}
	}
}
