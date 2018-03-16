package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.integration.vanilla.NullableItemStack;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.integration.vanilla.method.MethodsInventoryTransfer.extractHandler;

@IMethod.Inject(value = NullableItemStack.class, modId = RS.ID)
public class MethodExportItem extends BasicMethod<NullableItemStack> implements ITransferMethod {
	public MethodExportItem() {
		super("export", "function(to:string, [, limit:int][, toSlot:int]):int -- Export this item from the AE network to an inventory. Returns the amount transferred.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<NullableItemStack> context) {
		return super.canApply(context) && context.hasContext(INetwork.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<NullableItemStack> context, @Nonnull Object[] args) throws LuaException {
		final String toName = getString(args, 0);

		final int limit = optInt(args, 1, Integer.MAX_VALUE);
		final int toSlot = optInt(args, 2, -1);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return MethodResult.nextTick(() -> {
			IContext<NullableItemStack> baked = context.bake();

			// Find location to transfer to
			Object location = baked.getTransferLocation(toName);
			if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

			// Validate our location is valid
			IItemHandler to = extractHandler(location);
			if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");
			if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

			NullableItemStack toExtract = baked.getTarget();
			INetwork grid = baked.getContext(INetwork.class);

			// Extract the item from the inventory
			int extractLimit = Math.min(limit, toExtract.getFilledStack().getMaxStackSize());
			ItemStack toInsert = grid.extractItem(toExtract.getFilledStack(), extractLimit, false);

			// Attempt to insert into the appropriate inventory
			ItemStack remainder = toSlot <= 0
				? ItemHandlerHelper.insertItem(to, toInsert, false)
				: to.insertItem(toSlot - 1, toInsert, false);

			// If not everything could be inserted, replace back in the inventory
			if (!remainder.isEmpty()) {
				grid.insertItem(remainder, remainder.getCount(), false);
			}

			return MethodResult.result(toInsert.getCount() - remainder.getCount());
		});
	}
}
