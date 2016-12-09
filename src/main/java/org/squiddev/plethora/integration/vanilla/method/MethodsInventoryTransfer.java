package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

/**
 * Various methods for inventory transfer
 */
public class MethodsInventoryTransfer {
	@BasicMethod.Inject(
		value = IItemHandler.class,
		doc = "function(to:string, fromSlot:int[, limit:int] [, toSlot:int]):int -- Push items from this inventory to another inventory. Returns the amount transferred."
	)
	@MarkerInterfaces(ITransferMethod.class)
	public static MethodResult pushItems(final IUnbakedContext<IItemHandler> context, Object[] args) throws LuaException {
		final String toName = getString(args, 0);
		final int fromSlot = getInt(args, 1);

		final int limit = optInt(args, 2, Integer.MAX_VALUE);
		final int toSlot = optInt(args, 3, -1);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IItemHandler> baked = context.bake();
				IItemHandler from = baked.getTarget();

				// Find location to transfer to
				Object location = baked.getTransferLocation(toName);
				if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

				IItemHandler to = extractHandler(location);
				if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");

				// Validate slots
				assertBetween(fromSlot, 1, from.getSlots(), "From slot out of range (%s)");
				if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

				return MethodResult.result(moveItem(from, fromSlot - 1, to, toSlot - 1, limit));
			}
		});
	}

	@BasicMethod.Inject(
		value = IItemHandler.class,
		doc = "function(from:string, fromSlot:int[, limit:int] [, toSlot:int]):int -- Pull items to this inventory from another inventory. Returns the amount transferred."
	)
	@MarkerInterfaces(ITransferMethod.class)
	public static MethodResult pullItems(final IUnbakedContext<IItemHandler> context, Object[] args) throws LuaException {
		final String fromName = getString(args, 0);
		final int fromSlot = getInt(args, 1);

		final int limit = optInt(args, 2, Integer.MAX_VALUE);
		final int toSlot = optInt(args, 3, -1);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IItemHandler> baked = context.bake();
				IItemHandler to = baked.getTarget();

				// Find location to transfer to
				Object location = baked.getTransferLocation(fromName);
				if (location == null) throw new LuaException("Source '" + fromName + "' does not exist");

				IItemHandler from = extractHandler(location);
				if (from == null) throw new LuaException("Source '" + fromName + "' is not an inventory");

				// Validate slots
				assertBetween(fromSlot, 1, from.getSlots(), "From slot out of range (%s)");
				if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

				return MethodResult.result(moveItem(from, fromSlot - 1, to, toSlot - 1, limit));
			}
		});
	}

	@Nullable
	private static IItemHandler extractHandler(@Nonnull Object object) {
		for (Object child : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			if (child instanceof IItemHandler) return (IItemHandler) child;

			if (object instanceof ICapabilityProvider) {
				IItemHandler handler = ((ICapabilityProvider) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				if (handler != null) return handler;
			}
		}

		return null;
	}

	/**
	 * Move an item from one handler to another
	 *
	 * @param from     The handler to move from
	 * @param fromSlot The slot to move from
	 * @param to       The handler to move to
	 * @param toSlot   The slot to move to. Use any number < 0 to represent any slot.
	 * @param limit    The max number to move. {@link Integer#MAX_VALUE} for no limit.
	 * @return The actual number moved
	 */
	private static int moveItem(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, final int limit) {
		// See how much we can get out of this slot
		ItemStack extracted = from.extractItem(fromSlot, limit, true);
		if (extracted == null || extracted.stackSize <= 0) return 0;

		// Limit the amount to extract
		int extractCount = extracted.stackSize = Math.min(extracted.stackSize, limit);

		// Insert into the new handler
		ItemStack remainder = toSlot < 0 ? ItemHandlerHelper.insertItem(to, extracted, false) : to.insertItem(toSlot, extracted, false);

		// Calculate the amount which was inserted.
		int insertCount = remainder == null ? extractCount : extractCount - remainder.stackSize;

		// Technically this could fail. If it does then I'm going to cry.
		from.extractItem(fromSlot, insertCount, false);

		return insertCount;
	}
}
