package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.wrapper.Default;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * Various inventory methods which require interact with the world
 */
public class MethodsInventoryWorld {
	@PlethoraMethod(doc = "-- Drop an item on the ground. Returns the number of items dropped")
	public static int drop(
		@FromTarget IItemHandler handler, @FromContext(ContextKeys.TARGET) IWorldLocation location,
		int slot, @Default(defInt = Integer.MAX_VALUE) int limit, @Nullable EnumFacing direction
	) throws LuaException {
		if (limit <= 0) throw new LuaException("Limit must be > 0");
		assertBetween(slot, 1, handler.getSlots(), "Slot out of range (%s)");

		ItemStack stack = handler.extractItem(slot - 1, limit, false);
		return dropItem(location, stack, direction);
	}

	@PlethoraMethod(doc = "-- Drop an item on the ground. Returns the number of items dropped")
	public static int drop(
		@FromTarget ItemSlot slot, @FromContext(ContextKeys.TARGET) IWorldLocation location,
		@Default(defInt = Integer.MAX_VALUE) int limit, @Nullable EnumFacing direction
	) throws LuaException {
		if (limit <= 0) throw new LuaException("Limit must be > 0");

		ItemStack stack = slot.extract(limit);
		return dropItem(location, stack, direction);
	}

	private static int dropItem(IWorldLocation location, @Nonnull ItemStack stack, EnumFacing direction) {
		if (stack.isEmpty()) return 0;

		World world = location.getWorld();
		Vec3d pos = location.getLoc();
		if (direction != null) {
			pos = pos.add(new Vec3d(direction.getDirectionVec()).scale(0.75));
		}

		EntityItem entity = new EntityItem(world, pos.x, pos.y, pos.z, stack.copy());
		entity.motionX = 0;
		entity.motionY = 0;
		entity.motionZ = 0;
		entity.setDefaultPickupDelay();
		world.spawnEntity(entity);

		return stack.getCount();
	}

	private static final double SUCK_RADIUS = 1;

	@PlethoraMethod(doc = "-- Suck an item from the ground")
	public static int suck(
		@FromTarget IItemHandler handler, @FromContext(ContextKeys.TARGET) IWorldLocation location,
		@Default int slot, @Default(defInt = Integer.MAX_VALUE) int limit
	) throws LuaException {
		if (limit <= 0) throw new LuaException("Limit must be > 0");
		if (slot != -1) assertBetween(slot, 1, handler.getSlots(), "Slot out of range (%s)");

		World world = location.getWorld();
		BlockPos pos = location.getPos();

		AxisAlignedBB box = new AxisAlignedBB(
			pos.getX() + 0.5 - SUCK_RADIUS, pos.getY() + 0.5 - SUCK_RADIUS, pos.getZ() + 0.5 - SUCK_RADIUS,
			pos.getX() + 0.5 + SUCK_RADIUS, pos.getY() + 0.5 + SUCK_RADIUS, pos.getZ() + 0.5 + SUCK_RADIUS
		);

		int total = 0;
		int remaining = limit;
		for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE)) {
			ItemStack original = item.getItem();

			ItemStack toInsert = original.copy();
			if (toInsert.getCount() > remaining) toInsert.setCount(remaining);

			ItemStack rest = slot == -1
				? ItemHandlerHelper.insertItem(handler, toInsert, false)
				: handler.insertItem(slot - 1, toInsert, false);
			int inserted = rest.isEmpty() ? toInsert.getCount() : toInsert.getCount() - rest.getCount();
			remaining -= inserted;
			total += inserted;

			if (inserted >= original.getCount()) {
				item.setDead();
			} else {
				original.grow(-inserted);
				item.setItem(original);
			}

			if (remaining <= 0) break;
		}

		return total;
	}
}
