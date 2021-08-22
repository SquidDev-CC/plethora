package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.Sys;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.RangeInfo;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Scanner.rayTraceCost;

public final class MethodsScanner {
	private MethodsScanner() {
	}

	@PlethoraMethod(module = PlethoraModules.SCANNER_S, doc = "function():table -- Scan all blocks in the vicinity")
	public static MethodResult scan(
		IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@FromContext(PlethoraModules.SCANNER_S) RangeInfo range
	) throws LuaException {
		final World world = location.getWorld();
		final BlockPos pos = location.getPos();
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		return context.getCostHandler().await(range.getBulkCost(), () -> MethodResult.result(scan(world, x, y, z, range.getRange())));
	}

	private static List<Map<String, ?>> scan(World world, int x, int y, int z, int radius) {
		List<Map<String, ?>> result = new ArrayList<>();
		for (int oX = x - radius; oX <= x + radius; oX++) {
			for (int oY = y - radius; oY <= y + radius; oY++) {
				for (int oZ = z - radius; oZ <= z + radius; oZ++) {
					result.add(getBasicMeta(world, new BlockPos(oX, oY, oZ), new Vec3d(x, y,z)));
				}
			}
		}

		return result;
	}


	@PlethoraMethod(module = PlethoraModules.SCANNER_S,
			doc = "function(yaw: number, pitch: number, [hitLiquid: boolean[, ignoreBlockWithoutBoundingBox: boolean]): number, table -- Get a the distance and basic meta from a block using ray trace")
	public static MethodResult rayTrace(
			IContext<IModuleContainer> context,
			@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
			@FromContext(PlethoraModules.SCANNER_S) RangeInfo range,
			double yaw, double pitch, @Optional(defBool = false) boolean hitLiquid, @Optional(defBool = true) boolean ignoreBlockWithoutBoundingBox
	) throws LuaException {
		int radius = range.getRange();
		final double x = -Math.sin(yaw / 180.0f * Math.PI) * Math.cos(pitch / 180.0f * Math.PI);
		final double z = Math.cos(yaw / 180.0f * Math.PI) * Math.cos(pitch / 180.0f * Math.PI);
		final double y = -Math.sin(pitch / 180.0f * Math.PI);

		return context.getCostHandler().await(rayTraceCost*radius, () -> {


			Vec3d origin = location.getLoc();

			Vec3d positionEnd = origin.add(new Vec3d(x,y,z).scale(range.getRange()));

			System.out.println(origin + " - " + positionEnd);

			RayTraceResult rayTraceResult = location.getWorld().rayTraceBlocks(origin, positionEnd, hitLiquid, ignoreBlockWithoutBoundingBox, false);
			if (rayTraceResult == null) {
				return MethodResult.empty();

			} else {
				BlockPos blockPos = rayTraceResult.getBlockPos();

				return MethodResult.result(
						origin.distanceTo(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())),
						getBasicMeta(location.getWorld(), rayTraceResult.getBlockPos(), new Vec3d((int) origin.x, (int) origin.y, (int) origin.z)));
			}
		});
	}

	private static Map<String, ?> getBasicMeta(World world, BlockPos blockPos, Vec3d origin) {
		IBlockState block = world.getBlockState(blockPos).getActualState(world, blockPos);

		HashMap<String, Object> data = new HashMap<>(6);
		data.put("x", blockPos.getX() - origin.x);
		data.put("y", blockPos.getY() - origin.y);
		data.put("z", blockPos.getZ() - origin.z);

		ResourceLocation name = block.getBlock().getRegistryName();
		data.put("name", name == null ? "unknown" : name.toString());

		MetaBlockState.fillBasicMeta(data, block);

		return data;
	}


	@Nonnull
	@PlethoraMethod(module = PlethoraModules.SCANNER_S, doc = "-- Get metadata about a nearby block")
	public static TypedMeta<BlockReference, ?> getBlockMeta(
		IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@FromContext(PlethoraModules.SCANNER_S) RangeInfo range,
		int x, int y, int z
	) throws LuaException {

		int radius = range.getRange();
		assertBetween(x, -radius, radius, "X coordinate out of bounds (%s)");
		assertBetween(y, -radius, radius, "Y coordinate out of bounds (%s)");
		assertBetween(z, -radius, radius, "Z coordinate out of bounds (%s)");

		return context
			.makeChild(new BlockReference(new WorldLocation(location.getWorld(), location.getPos().add(x, y, z))))
			.getMeta();
	}
}
