package org.squiddev.plethora.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
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
import java.util.List;

import static org.squiddev.plethora.modules.ItemModule.SENSOR_RADIUS;

public final class SensorModule {
	public static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.SENSOR);

	@Method(IModule.class)
	public static final class ScanEntitiesMethod extends ModuleMethod {
		public ScanEntitiesMethod() {
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

			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
				x - SENSOR_RADIUS, y - SENSOR_RADIUS, z - SENSOR_RADIUS,
				x + SENSOR_RADIUS, y + SENSOR_RADIUS, z + SENSOR_RADIUS
			));

			int i = 0;
			HashMap<Integer, Object> map = Maps.newHashMap();
			for (Entity entity : entities) {
				HashMap<String, Object> data = Maps.newHashMap();
				data.put("x", entity.posX - x);
				data.put("y", entity.posY - y);
				data.put("z", entity.posZ - z);
				data.put("id", entity.getUniqueID());
				data.put("name", entity.getName());

				map.put(++i, data);
			}

			return new Object[]{map};
		}
	}
}
