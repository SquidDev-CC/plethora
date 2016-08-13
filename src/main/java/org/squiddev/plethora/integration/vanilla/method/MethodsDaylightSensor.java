package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import java.util.concurrent.Callable;

/**
 * Various methods for interacting with the daylight sensor module.
 * Providers information about light levels in the area.
 *
 * TODO: Convert to use TargetedModuleObjectMethod once I've got generation working
 */
public class MethodsDaylightSensor {
	@TargetedModuleMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class,
		doc = "function():boolean -- Whether this world has a sky"
	)
	public static MethodResult hasSky(final IUnbakedContext<IModule> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				World world = context.bake().getContext(IWorldLocation.class).getWorld();
				return MethodResult.result(!world.provider.getHasNoSky());
			}
		});
	}

	@TargetedModuleMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class,
		doc = "function():int -- The light level from the sun"
	)
	public static MethodResult getSkyLight(final IUnbakedContext<IModule> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IWorldLocation location = context.bake().getContext(IWorldLocation.class);
				World world = location.getWorld();
				if (world.provider.getHasNoSky()) {
					throw new LuaException("The world has no sky");
				} else {
					BlockPos pos = location.getPos();
					return MethodResult.result(world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted());
				}
			}
		});
	}

	@TargetedModuleMethod.Inject(
		module = IntegrationVanilla.daylightSensor, target = IWorldLocation.class,
		doc = "function():int -- The light level from surrounding blocks"
	)
	public static MethodResult getBlockLight(final IUnbakedContext<IModule> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IWorldLocation location = context.bake().getContext(IWorldLocation.class);
				World world = location.getWorld();
				BlockPos pos = location.getPos();
				return MethodResult.result(world.getLightFor(EnumSkyBlock.BLOCK, pos));
			}
		});
	}
}
