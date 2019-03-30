package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.me.helpers.MachineSource;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ITransferMethod;
import org.squiddev.plethora.api.method.MarkerInterfaces;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.integration.vanilla.method.MethodsInventoryTransfer.extractHandler;

public final class MethodExportItem {
	private MethodExportItem() {
	}

	@PlethoraMethod(
		modId = AppEng.MOD_ID,
		doc = "-- Export this item from the AE network to an inventory. Returns the amount transferred."
	)
	@MarkerInterfaces(ITransferMethod.class)
	public static long export(
		IContext<IAEItemStack> baked, @FromContext IGrid grid, @FromContext IActionHost host,
		String toName, @Optional(defInt = Integer.MAX_VALUE) int limit, @Optional int toSlot
	) throws LuaException {
		// Find location to transfer to
		Object location = baked.getTransferLocation(toName);
		if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

		// Validate our location is valid
		IItemHandler to = extractHandler(location);
		if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");

		if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");
		if (limit <= 0) throw new LuaException("Limit must be > 0");

		// Find the stack to extract
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		MachineSource source = new MachineSource(host);

		// Extract said item
		IAEItemStack toExtract = baked.getTarget().copy();
		toExtract.setStackSize(Math.min(limit, toExtract.getDefinition().getMaxStackSize()));
		toExtract = storageGrid.getInventory(channel).extractItems(toExtract, Actionable.MODULATE, source);

		// Attempt to insert into the appropriate inventory
		ItemStack toInsert = toExtract.createItemStack();
		ItemStack remainder = toSlot <= 0
			? ItemHandlerHelper.insertItem(to, toInsert, false)
			: to.insertItem(toSlot - 1, toInsert, false);

		// If not everything could be inserted, replace back in the inventory
		if (!remainder.isEmpty()) {
			storageGrid.getInventory(channel).injectItems(channel.createStack(remainder), Actionable.MODULATE, source);
		}

		return toExtract.getStackSize() - remainder.getCount();
	}
}
