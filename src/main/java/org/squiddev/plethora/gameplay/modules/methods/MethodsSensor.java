package org.squiddev.plethora.gameplay.modules.methods;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.api.method.LuaList;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.squiddev.plethora.gameplay.ConfigGameplay.Sensor.radius;

public final class MethodsSensor {
	private MethodsSensor() {
	}

	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "-- Scan for entities in the vicinity")
	public static Map<Integer, HashMap<String, Object>> sense(@FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		final World world = location.getWorld();
		final BlockPos pos = location.getPos();

		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getBox(pos), DEFAULT_PREDICATE::test);

		return LuaList.of(entities, x -> MetaEntity.getBasicProperties(x, location)).asMap();
	}

	@Optional
	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "-- Find a nearby entity by UUID")
	public static Map<String, ?> getMetaByID(
		IContext<IModuleContainer> context, @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		UUID id
	) {
		Entity entity = findEntityByUUID(location, id);
		return entity == null ? null : context.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta();
	}

	@Optional
	@PlethoraMethod(module = PlethoraModules.SENSOR_S, doc = "-- Find a nearby entity by name")
	public static Map<String, ?> getMetaByName(
		IContext<IModuleContainer> context, @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		String name
	) {
		Entity entity = findEntityByName(location, name);
		return entity == null ? null : context.makeChild(entity, Reference.bounded(entity, location, radius)).getMeta();
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
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()),
			entity -> DEFAULT_PREDICATE.test(entity) && entity.getUniqueID().equals(uuid));
		return entities.isEmpty() ? null : entities.get(0);
	}

	@Nullable
	private static Entity findEntityByName(IWorldLocation location, String name) {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()),
			entity -> DEFAULT_PREDICATE.test(entity) && Helpers.getName(entity).equals(name));

		return entities.isEmpty() ? null : entities.get(0);
	}

	private static final Predicate<Entity> DEFAULT_PREDICATE = entity ->
		entity != null && entity.isEntityAlive() && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator());
}
