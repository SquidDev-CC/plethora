package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.squiddev.plethora.ArgumentHelper.getString;
import static org.squiddev.plethora.modules.ItemModule.SENSOR_RADIUS;

public final class MethodsSensor {
	@Method(IModule.class)
	public static final class ScanEntitiesMethod extends TargetedModuleMethod<IWorldLocation> {
		public ScanEntitiesMethod() {
			super("scan", true, PlethoraModules.SENSOR, IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
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
	public static final class GetMetaUUIDMethod extends TargetedModuleMethod<IWorldLocation> {
		public GetMetaUUIDMethod() {
			super("getMetaByID", true, PlethoraModules.SENSOR, IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByUUID(location, args);
			return new Object[]{PlethoraAPI.instance().metaRegistry().getMeta(entity)};
		}
	}

	@Method(IModule.class)
	public static final class GetMetaNameMethod extends TargetedModuleMethod<IWorldLocation> {
		public GetMetaNameMethod() {
			super("getMetaByName", true, PlethoraModules.SENSOR, IWorldLocation.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			Entity entity = findEntityByName(location, args);
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

	private static Entity findEntityByUUID(IWorldLocation location, Object[] args) throws LuaException {
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

	private static Entity findEntityByName(IWorldLocation location, Object[] args) throws LuaException {
		String name = getString(args, 0);

		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (MetaEntity.getName(entity).equals(name)) return entity;
		}

		throw new LuaException("No such entity");
	}
}
