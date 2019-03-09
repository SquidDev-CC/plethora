package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ITransferMethod;
import org.squiddev.plethora.api.method.MarkerInterfaces;
import org.squiddev.plethora.api.method.gen.Default;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * Various methods for inventory transfer
 */
public class MethodsInventoryTransfer {
	@PlethoraMethod(doc = "-- Push items from this inventory to another inventory. Returns the amount transferred.")
	@MarkerInterfaces(ITransferMethod.class) // TODO: This!
	public static int pushItems(
		IContext<IItemHandler> context,
		String toName, int fromSlot, @Default(defInt = Integer.MAX_VALUE) int limit, @Default int toSlot
	) throws LuaException {
		IItemHandler from = context.getTarget();

		// Find location to transfer to
		Object location = context.getTransferLocation(toName);
		if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

		IItemHandler to = extractHandler(location);
		if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");

		// Validate slots
		if (limit <= 0) throw new LuaException("Limit must be > 0");
		assertBetween(fromSlot, 1, from.getSlots(), "From slot out of range (%s)");
		if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

		return moveItem(from, fromSlot - 1, to, toSlot - 1, limit);
	}

	@PlethoraMethod(doc = "-- Pull items to this inventory from another inventory. Returns the amount transferred.")
	@MarkerInterfaces(ITransferMethod.class)
	public static int pullItems(
		IContext<IItemHandler> context,
		String fromName, int fromSlot, @Default(defInt = Integer.MAX_VALUE) int limit, @Default int toSlot
	) throws LuaException {
		IItemHandler to = context.getTarget();

		// Find location to transfer to
		Object location = context.getTransferLocation(fromName);
		if (location == null) throw new LuaException("Source '" + fromName + "' does not exist");

		IItemHandler from = extractHandler(location);
		if (from == null) throw new LuaException("Source '" + fromName + "' is not an inventory");

		// Validate slots
		if (limit <= 0) throw new LuaException("Limit must be > 0");
		assertBetween(fromSlot, 1, from.getSlots(), "From slot out of range (%s)");
		if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

		return moveItem(from, fromSlot - 1, to, toSlot - 1, limit);
	}

	@Nullable
	public static IItemHandler extractHandler(@Nonnull Object object) {
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
		if (extracted.isEmpty()) return 0;

		// Limit the amount to extract
		int extractCount = Math.min(extracted.getCount(), limit);
		extracted.setCount(extractCount);

		// Insert into the new handler
		ItemStack remainder = toSlot < 0 ? ItemHandlerHelper.insertItem(to, extracted, false) : to.insertItem(toSlot, extracted, false);

		// Calculate the amount which was inserted.
		int insertCount = remainder.isEmpty() ? extractCount : extractCount - remainder.getCount();

		// Technically this could fail. If it does then I'm going to cry.
		from.extractItem(fromSlot, insertCount, false);

		return insertCount;
	}
}
