package org.squiddev.plethora.gameplay.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static org.squiddev.plethora.api.method.ArgumentHelper.getUUID;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Sensor.radius;

public final class MethodsSensor {
	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.SENSOR_S, target = IWorldLocation.class, worldThread = true,
		doc = "function():table -- Scan for entities in the vicinity"
	)
	public static Object[] sense(@Nonnull IWorldLocation location, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final World world = location.getWorld();
		final BlockPos pos = location.getPos();

		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getBox(pos));

		int i = 0;
		HashMap<Integer, Object> map = Maps.newHashMap();
		for (Entity entity : entities) {
			Map<Object, Object> data = MetaEntity.getBasicProperties(entity, location);
			map.put(++i, data);
		}

		return new Object[]{map};
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.SENSOR_S,
		target = IWorldLocation.class,
		doc = "function(id:string):table|nil -- Find a nearby entity by UUID"
	)
	public static MethodResult getMetaByID(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final UUID uuid = getUUID(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IModuleContainer> baked = context.bake();
			IWorldLocation location = baked.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			Entity entity = findEntityByUUID(location, uuid);
			if (entity == null) {
				return MethodResult.empty();
			} else {
				return MethodResult.result(baked.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta());

			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.SENSOR_S,
		target = IWorldLocation.class,
		doc = "function(name:string):table|nil -- Find a nearby entity by name"
	)
	@Nonnull
	public static MethodResult getMetaByName(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final String name = getString(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IModuleContainer> baked = context.bake();
			IWorldLocation location = baked.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			Entity entity = findEntityByName(location, name);
			if (entity == null) {
				return MethodResult.empty();
			} else {
				return MethodResult.result(baked.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta());

			}
		});
	}

	private static AxisAlignedBB getBox(BlockPos pos) {
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new AxisAlignedBB(
			x - radius, y - radius, z - radius,
			x + radius, y + radius, z + radius
		);
	}

	@Nullable
	private static Entity findEntityByUUID(IWorldLocation location, UUID uuid) {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (entity.getUniqueID().equals(uuid)) return entity;
		}

		return null;
	}

	@Nullable
	private static Entity findEntityByName(IWorldLocation location, String name) {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (Helpers.getName(entity).equals(name)) return entity;
		}

		return null;
	}
}
