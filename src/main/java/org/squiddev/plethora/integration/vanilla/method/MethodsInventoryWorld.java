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
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

/**
 * Various inventory methods which require interact with the world
 */
public class MethodsInventoryWorld {
	@IMethod.Inject(IItemHandler.class)
	public static class MethodItemHandlerDrop extends BasicMethod<IItemHandler> {
		public MethodItemHandlerDrop() {
			super("drop", "function(slot:int[, limit:int][, direction:string]):int -- Drop an item on the ground. Returns the number of items dropped");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<IItemHandler> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
			final int slot = getInt(args, 0);
			final int limit = optInt(args, 1, Integer.MAX_VALUE);

			if (limit <= 0) throw new LuaException("Limit must be > 0");

			final EnumFacing direction = optEnum(args, 2, EnumFacing.class, null);

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					IContext<IItemHandler> baked = context.bake();
					IItemHandler handler = baked.getTarget();

					assertBetween(slot, 1, handler.getSlots(), "Slot out of range (%s)");

					ItemStack stack = handler.extractItem(slot - 1, limit, false);
					return MethodResult.result(dropItem(baked.getContext(IWorldLocation.class), stack, direction));
				}
			});
		}
	}

	@IMethod.Inject(ItemSlot.class)
	public static class MethodItemDrop extends BasicMethod<ItemSlot> {
		public MethodItemDrop() {
			super("drop", "function([limit:int][, direction:string]):int -- Drop an item on the ground. Returns the number of items dropped");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemSlot> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<ItemSlot> context, @Nonnull Object[] args) throws LuaException {
			final int limit = optInt(args, 0, Integer.MAX_VALUE);
			if (limit <= 0) throw new LuaException("Limit must be > 0");
			final EnumFacing direction = optEnum(args, 1, EnumFacing.class, null);

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					IContext<ItemSlot> baked = context.bake();
					ItemSlot slot = baked.getTarget();

					ItemStack stack = slot.extract(limit);
					return MethodResult.result(dropItem(baked.getContext(IWorldLocation.class), stack, direction));
				}
			});
		}
	}

	private static int dropItem(IWorldLocation location, @Nonnull ItemStack stack, EnumFacing direction) {
		if (stack.isEmpty()) return 0;

		World world = location.getWorld();
		Vec3d pos = location.getLoc();
		if (direction != null) {
			pos = pos.addVector(
				direction.getFrontOffsetX() * 0.6,
				direction.getFrontOffsetY() * 0.6,
				direction.getFrontOffsetZ() * 0.6
			);
		}

		EntityItem entity = new EntityItem(world, pos.xCoord, pos.yCoord, pos.zCoord, stack.copy());
		entity.setDefaultPickupDelay();
		world.spawnEntity(entity);

		return stack.getCount();
	}

	@IMethod.Inject(IItemHandler.class)
	public static class MethodItemHandlerSuck extends BasicMethod<IItemHandler> {
		private static final double RADIUS = 1;

		public MethodItemHandlerSuck() {
			super("suck", "function([slot:int][, limit:int]):int -- Suck an item from the ground");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<IItemHandler> context) {
			return super.canApply(context) && context.hasContext(IWorldLocation.class);
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
			final int slot = optInt(args, 0, -1);

			final int limit = optInt(args, 1, Integer.MAX_VALUE);
			if (limit <= 0) throw new LuaException("Limit must be > 0");

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					IContext<IItemHandler> baked = context.bake();
					IItemHandler handler = baked.getTarget();

					if (slot != -1) assertBetween(slot, 1, handler.getSlots(), "Slot out of range (%s)");

					IWorldLocation location = baked.getContext(IWorldLocation.class);
					World world = location.getWorld();
					BlockPos pos = location.getPos();

					AxisAlignedBB box = new AxisAlignedBB(
						pos.getX() + 0.5 - RADIUS, pos.getY() + 0.5 - RADIUS, pos.getZ() + 0.5 - RADIUS,
						pos.getX() + 0.5 + RADIUS, pos.getY() + 0.5 + RADIUS, pos.getZ() + 0.5 + RADIUS
					);

					int total = 0;
					int remaining = limit;
					for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE)) {
						ItemStack original = item.getEntityItem();

						ItemStack toInsert = original.copy();
						if (toInsert.getCount() > remaining) toInsert.setCount(remaining);

						ItemStack rest = slot == -1 ? ItemHandlerHelper.insertItem(handler, toInsert, false) : handler.insertItem(slot - 1, toInsert, false);
						int inserted = rest.isEmpty() ? toInsert.getCount() : toInsert.getCount() - rest.getCount();
						remaining -= inserted;
						total += inserted;

						if (inserted >= original.getCount()) {
							item.setDead();
						} else {
							original.grow(-inserted);
							item.setEntityItemStack(original);
						}

						if (remaining <= 0) break;
					}

					return MethodResult.result(total);
				}
			});
		}
	}
}
