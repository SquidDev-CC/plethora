package org.squiddev.plethora.gameplay.modules.methods;

import com.google.common.base.Predicate;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.RangeInfo;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MethodsSensor {
	private MethodsSensor() {
	}

	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "function():table -- Scan for entities in the vicinity")
	public static MethodResult sense(
		IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@FromContext(PlethoraModules.SENSOR_S) RangeInfo range
	) throws LuaException {
		final World world = location.getWorld();
		final BlockPos pos = location.getPos();

		return context.getCostHandler().await(range.getBulkCost(), () -> {
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getBox(pos, range.getRange()), DEFAULT_PREDICATE::test);
			return MethodResult.result(LuaList.of(entities, x -> MetaEntity.getBasicProperties(x, location)).asMap());
		});
	}

	@Optional
	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "-- Find a nearby entity by UUID")
	public static Map<String, ?> getMetaByID(
		IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@FromContext(PlethoraModules.SENSOR_S) RangeInfo range,
		UUID id
	) {
		int radius = range.getRange();
		Entity entity = findEntityByUUID(location, radius, id);
		return entity == null ? null : context.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta();
	}

	@Optional
	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "-- Find a nearby entity by name")
	public static Map<String, ?> getMetaByName(
		IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@FromContext(PlethoraModules.SENSOR_S) RangeInfo range,
		String name
	) {
		int radius = range.getRange();
		Entity entity = findEntityByName(location, radius, name);
		return entity == null ? null : context.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta();
	}

	private static AxisAlignedBB getBox(BlockPos pos, int radius) {
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new AxisAlignedBB(
			x - radius, y - radius, z - radius,
			x + radius, y + radius, z + radius
		);
	}

	@Nullable
	private static Entity findEntityByUUID(IWorldLocation location, int radius, UUID uuid) {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos(), radius),
			entity -> DEFAULT_PREDICATE.test(entity) && entity.getUniqueID().equals(uuid));
		return entities.isEmpty() ? null : entities.get(0);
	}

	@Nullable
	private static Entity findEntityByName(IWorldLocation location, int radius, String name) {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos(), radius),
			entity -> DEFAULT_PREDICATE.test(entity) && Helpers.getName(entity).equals(name));

		return entities.isEmpty() ? null : entities.get(0);
	}

	private static final Predicate<Entity> DEFAULT_PREDICATE = entity ->
		entity != null && entity.isEntityAlive() && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator());
}
