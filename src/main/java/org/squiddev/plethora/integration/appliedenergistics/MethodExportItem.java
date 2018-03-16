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
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.integration.vanilla.method.MethodsInventoryTransfer.extractHandler;

@IMethod.Inject(value = IAEItemStack.class, modId = AppEng.MOD_ID)
public class MethodExportItem extends BasicMethod<IAEItemStack> implements ITransferMethod {
	public MethodExportItem() {
		super("export", "function(to:string, [, limit:int][, toSlot:int]):int -- Export this item from the AE network to an inventory. Returns the amount transferred.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IAEItemStack> context) {
		return super.canApply(context) && context.hasContext(IActionHost.class) && context.hasContext(IGrid.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<IAEItemStack> context, @Nonnull Object[] args) throws LuaException {
		final String toName = getString(args, 0);

		final int limit = optInt(args, 1, Integer.MAX_VALUE);
		final int toSlot = optInt(args, 2, -1);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return MethodResult.nextTick(() -> {
			IContext<IAEItemStack> baked = context.bake();

			// Find location to transfer to
			Object location = baked.getTransferLocation(toName);
			if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

			// Validate our location is valid
			IItemHandler to = extractHandler(location);
			if (to == null) throw new LuaException("Target '" + toName + "' is not an inventory");
			if (toSlot != -1) assertBetween(toSlot, 1, to.getSlots(), "To slot out of range (%s)");

			// Find the stack to extract
			IGrid grid = baked.getContext(IGrid.class);
			IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			MachineSource source = new MachineSource(baked.getContext(IActionHost.class));

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

			return MethodResult.result(toExtract.getStackSize() - remainder.getCount());
		});
	}
}
