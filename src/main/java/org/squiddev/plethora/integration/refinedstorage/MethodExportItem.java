package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.util.Action;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ITransferMethod;
import org.squiddev.plethora.api.method.MarkerInterfaces;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.NullableItemStack;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.integration.vanilla.method.MethodsInventoryTransfer.extractHandler;

public final class MethodExportItem {
	@PlethoraMethod(modId = RS.ID, doc = "-- Export this item from the RS network to an inventory. Returns the amount transferred.")
	@MarkerInterfaces(ITransferMethod.class)
	public static int export(
		IContext<NullableItemStack> context, INetworkNode node,
		String toName, @Optional(defInt = Integer.MAX_VALUE) int limit, @Optional int toSlot
	) throws LuaException {
		if (limit <= 0) throw new LuaException("Limit must be > 0");

		// Find location to transfer to
		Object location = context.getTransferLocation(toName);
		if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

		// Validate our location is valid
		IItemHandler to = extractHandler(location);
		if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");
		if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

		NullableItemStack toExtract = context.getTarget();
		INetwork network = node.getNetwork();
		if (network == null) throw new LuaException("Cannot find network");

		// Extract the item from the inventory
		int extractLimit = Math.min(limit, toExtract.getFilledStack().getMaxStackSize());
		ItemStack toInsert = network.extractItem(toExtract.getFilledStack(), extractLimit, Action.PERFORM);

		if (toInsert == null || toInsert.isEmpty()) return 0;

		// Attempt to insert into the appropriate inventory
		ItemStack remainder = toSlot <= 0
			? ItemHandlerHelper.insertItem(to, toInsert, false)
			: to.insertItem(toSlot - 1, toInsert, false);

		// If not everything could be inserted, replace back in the inventory
		if (!remainder.isEmpty()) {
			network.insertItem(remainder, remainder.getCount(), Action.PERFORM);
		}

		return toInsert.getCount() - remainder.getCount();
	}
}
