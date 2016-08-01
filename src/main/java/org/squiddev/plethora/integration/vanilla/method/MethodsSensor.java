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
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getString;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Modules.sensorRadius;

public final class MethodsSensor {
	@IMethod.Inject(IModule.class)
	public static final class ScanEntitiesMethod extends TargetedModuleObjectMethod<IWorldLocation> {
		public ScanEntitiesMethod() {
			super("scan", PlethoraModules.SENSOR, IWorldLocation.class, true, "function():table -- Scan for entities in the vicinity");
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

	@IMethod.Inject(IModule.class)
	public static final class GetMetaUUIDMethod extends TargetedModuleMethod<IWorldLocation> {
		public GetMetaUUIDMethod() {
			super("getMetaByID", PlethoraModules.SENSOR, IWorldLocation.class, "function():table|nil -- Find a nearby entity by UUID");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final UUID uuid;
			try {
				uuid = UUID.fromString(getString(args, 0));
			} catch (IllegalArgumentException e) {
				throw new LuaException("Invalid UUID");
			}

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					Entity entity = findEntityByUUID(context.bake().getContext(IWorldLocation.class), uuid);
					if (entity == null) {
						return MethodResult.empty();
					} else {
						return MethodResult.result(PlethoraAPI.instance().metaRegistry().getMeta(entity));
					}
				}
			});
		}
	}

	@IMethod.Inject(IModule.class)
	public static final class GetMetaNameMethod extends TargetedModuleMethod<IWorldLocation> {
		public GetMetaNameMethod() {
			super("getMetaByName", PlethoraModules.SENSOR, IWorldLocation.class, "function():table|nil -- Find a nearby entity by name");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final String name = getString(args, 0);

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					Entity entity = findEntityByName(context.bake().getContext(IWorldLocation.class), name);
					if (entity == null) {
						return MethodResult.empty();
					} else {
						return MethodResult.result(PlethoraAPI.instance().metaRegistry().getMeta(entity));
					}
				}
			});
		}
	}

	private static AxisAlignedBB getBox(BlockPos pos) {
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new AxisAlignedBB(
			x - sensorRadius, y - sensorRadius, z - sensorRadius,
			x + sensorRadius, y + sensorRadius, z + sensorRadius
		);
	}

	@Nullable
	private static Entity findEntityByUUID(IWorldLocation location, UUID uuid) throws LuaException {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (entity.getUniqueID().equals(uuid)) return entity;
		}

		return null;
	}

	@Nullable
	private static Entity findEntityByName(IWorldLocation location, String name) throws LuaException {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (MetaEntity.getName(entity).equals(name)) return entity;
		}

		return null;
	}
}
