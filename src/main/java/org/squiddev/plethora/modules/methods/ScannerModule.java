package org.squiddev.plethora.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.modules.ItemModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.ArgumentHelper.getInt;
import static org.squiddev.plethora.modules.ItemModule.SCANNER_RADIUS;

public final class ScannerModule {
	private static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.SCANNER);

	@Method(IModule.class)
	public static final class ScanBlocksMethod extends ModuleMethod {
		public ScanBlocksMethod() {
			super("scan", true, MODULE);
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final IWorldLocation location = context.getContext(IWorldLocation.class);
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
	public static final class MetaBlockMethod extends ModuleMethod {
		public MetaBlockMethod() {
			super("getBlockMeta", true, MODULE);
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			int x = getInt(args, 0);
			int y = getInt(args, 1);
			int z = getInt(args, 2);

			validatePosition(x, y, z);

			IWorldLocation location = context.getContext(IWorldLocation.class);
			BlockPos pos = location.getPos().add(x, y, z);
			World world = location.getWorld();

			IBlockState block = world.getBlockState(pos);
			IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
			Map<Object, Object> meta = registry.getMeta(block);

			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				meta.putAll(registry.getMeta(block));
			}

			return new Object[]{meta};
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
