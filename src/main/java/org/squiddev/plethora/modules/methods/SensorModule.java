package org.squiddev.plethora.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.modules.ItemModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.squiddev.plethora.ArgumentHelper.getString;
import static org.squiddev.plethora.modules.ItemModule.SENSOR_RADIUS;

public final class SensorModule {
	private static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.SENSOR);

	private abstract static class SensorMethod extends ModuleMethod {
		public SensorMethod(String name) {
			super(name, true, MODULE);
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(WorldLocation.class);
		}
	}

	@Method(IModule.class)
	public static final class ScanEntitiesMethod extends SensorMethod {
		public ScanEntitiesMethod() {
			super("scan");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final WorldLocation location = context.getContext(WorldLocation.class);
			final World world = location.getWorld();
			final BlockPos pos = location.getPos();
			final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getBox(pos));

			int i = 0;
			HashMap<Integer, Object> map = Maps.newHashMap();
			for (Entity entity : entities) {
				Map<Object, Object> data = MetaEntity.getBasicProperties(entity);
				data.put("x", entity.posX - x);
				data.put("y", entity.posY - y);
				data.put("z", entity.posZ - z);
				map.put(++i, data);
			}

			return new Object[]{map};
		}
	}

	@Method(IModule.class)
	public static final class GetUUIDMethod extends SensorMethod {
		public GetUUIDMethod() {
			super("getByID");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByUUID(context, args);
			return new Object[]{PlethoraAPI.instance().methodRegistry().getObject(
				context.makeChild(Reference.bounded(entity, context.getContext(WorldLocation.class), SENSOR_RADIUS))
			)};
		}
	}

	@Method(IModule.class)
	public static final class GetNameMethod extends SensorMethod {
		public GetNameMethod() {
			super("getByName");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByName(context, args);
			return new Object[]{PlethoraAPI.instance().methodRegistry().getObject(
				context.makeChild(Reference.entity(entity))
			)};
		}
	}

	@Method(IModule.class)
	public static final class GetMetaUUIDMethod extends SensorMethod {
		public GetMetaUUIDMethod() {
			super("getMetaByID");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByUUID(context, args);
			return new Object[]{PlethoraAPI.instance().metaRegistry().getMeta(entity)};
		}
	}

	@Method(IModule.class)
	public static final class GetMetaNameMethod extends SensorMethod {
		public GetMetaNameMethod() {
			super("getMetaByName");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByName(context, args);
			return new Object[]{PlethoraAPI.instance().metaRegistry().getMeta(entity)};
		}
	}

	private static AxisAlignedBB getBox(BlockPos pos) {
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new AxisAlignedBB(
			x - SENSOR_RADIUS, y - SENSOR_RADIUS, z - SENSOR_RADIUS,
			x + SENSOR_RADIUS, y + SENSOR_RADIUS, z + SENSOR_RADIUS
		);
	}

	private static Entity findEntityByUUID(IContext<IModule> context, Object[] args) throws LuaException {
		WorldLocation location = context.getContext(WorldLocation.class);

		UUID uuid;
		try {
			uuid = UUID.fromString(getString(args, 0));
		} catch (IllegalArgumentException e) {
			throw new LuaException("Invalid UUID");
		}

		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (entity.getUniqueID().equals(uuid)) return entity;
		}

		throw new LuaException("No such entity");
	}

	private static Entity findEntityByName(IContext<IModule> context, Object[] args) throws LuaException {
		WorldLocation location = context.getContext(WorldLocation.class);

		String name = getString(args, 0);

		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (MetaEntity.getName(entity).equals(name)) return entity;
		}

		throw new LuaException("No such entity");
	}
}
