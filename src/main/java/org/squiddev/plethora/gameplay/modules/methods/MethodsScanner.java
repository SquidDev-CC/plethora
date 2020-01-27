package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
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
					BlockPos subPos = new BlockPos(oX, oY, oZ);
					IBlockState block = world.getBlockState(subPos).getActualState(world, subPos);

					HashMap<String, Object> data = new HashMap<>(6);
					data.put("x", oX - x);
					data.put("y", oY - y);
					data.put("z", oZ - z);

					ResourceLocation name = block.getBlock().getRegistryName();
					data.put("name", name == null ? "unknown" : name.toString());

					MetaBlockState.fillBasicMeta(data, block);

					result.add(data);
				}
			}
		}

		return result;
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
