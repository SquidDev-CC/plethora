package org.squiddev.plethora.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.modules.ItemModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static org.squiddev.plethora.modules.ItemModule.SCANNER_RADIUS;

public final class ScannerModule {
	public static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.SCANNER);

	@Method(IModule.class)
	public static final class ScanBlocksMethod extends ModuleMethod {
		public ScanBlocksMethod() {
			super("scan", true, MODULE);
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(WorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final WorldLocation location = context.getContext(WorldLocation.class);
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
}
