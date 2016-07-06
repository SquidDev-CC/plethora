package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.ArgumentHelper.getInt;
import static org.squiddev.plethora.gameplay.modules.ItemModule.SCANNER_RADIUS;

public final class MethodsScanner {
	@Method(IModule.class)
	public static final class MethodScanBlocks extends TargetedModuleObjectMethod<IWorldLocation> {
		public MethodScanBlocks() {
			super("scan", true, PlethoraModules.SCANNER, IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final World world = location.getWorld();
			final BlockPos pos = location.getPos();
			final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

			int i = 0;
			HashMap<Integer, Object> map = Maps.newHashMap();
			for (int oX = x - SCANNER_RADIUS; oX <= x + SCANNER_RADIUS; oX++) {
				for (int oY = y - SCANNER_RADIUS; oY <= y + SCANNER_RADIUS; oY++) {
					for (int oZ = z - SCANNER_RADIUS; oZ <= z + SCANNER_RADIUS; oZ++) {
						BlockPos newPos = new BlockPos(oX, oY, oZ);
						IBlockState block = world.getBlockState(newPos);

						HashMap<String, Object> data = Maps.newHashMap();
						data.put("x", oX - x);
						data.put("y", oY - y);
						data.put("z", oZ - z);
						String name = block.getBlock().getRegistryName();
						data.put("name", name == null ? "unknown" : name);

						map.put(++i, data);
					}
				}
			}

			return new Object[]{map};
		}
	}

	@Method(IModule.class)
	public static final class MethodMetaBlock extends TargetedModuleMethod<IWorldLocation> {
		public MethodMetaBlock() {
			super("getBlockMeta", PlethoraModules.SCANNER, IWorldLocation.class);
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final int x = getInt(args, 0);
			final int y = getInt(args, 1);
			final int z = getInt(args, 2);

			validatePosition(x, y, z);

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					IWorldLocation location = context.bake().getContext(IWorldLocation.class);
					BlockPos pos = location.getPos().add(x, y, z);
					World world = location.getWorld();

					IBlockState block = world.getBlockState(pos);
					IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
					Map<Object, Object> meta = registry.getMeta(block);

					TileEntity te = world.getTileEntity(pos);
					if (te != null) {
						meta.putAll(registry.getMeta(block));
					}

					return MethodResult.result(meta);
				}
			});
		}
	}

	private static void validatePosition(int x, int y, int z) throws LuaException {
		if (x < -SCANNER_RADIUS || x > SCANNER_RADIUS) {
			throw new LuaException("X coordinate out of bounds (+-" + SCANNER_RADIUS + ")");
		}
		if (y < -SCANNER_RADIUS || y > SCANNER_RADIUS) {
			throw new LuaException("Y coordinate out of bounds (+-" + SCANNER_RADIUS + ")");
		}
		if (z < -SCANNER_RADIUS || z > SCANNER_RADIUS) {
			throw new LuaException("Z coordinate out of bounds (+-" + SCANNER_RADIUS + ")");
		}
	}
}
