package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.me.helpers.MachineSource;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

@IMethod.Inject(value = IAEItemStack.class, modId = AppEng.MOD_ID)
public class MethodCraftItem extends BasicMethod<IAEItemStack> {
	public MethodCraftItem() {
		super("craft", "(count:int):boolean, table -- Craft this item, returning if the item could be crafted and a " +
			"reference to the crafting task.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IAEItemStack> context) {
		return super.canApply(context) && context.hasContext(IActionHost.class) && context.hasContext(IGridNode.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<IAEItemStack> context, @Nonnull Object[] args) throws LuaException {
		final int quantity = getInt(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IAEItemStack> baked = context.bake();

			IGridNode node = baked.getContext(IGridNode.class);
			IActionHost host = baked.getContext(IActionHost.class);

			IGrid grid = node.getGrid();
			ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);

			IAEItemStack toCraft = baked.getTarget().copy();
			toCraft.setStackSize(quantity);

			CraftingResult result = new CraftingResult(grid, baked.getContext(IComputerAccess.class), host);
			crafting.beginCraftingJob(node.getWorld(), grid, new MachineSource(host), toCraft, result.getCallback());

			return MethodResult.result(context.makeChild(Reference.id(result)).getObject());
		});
	}
}
