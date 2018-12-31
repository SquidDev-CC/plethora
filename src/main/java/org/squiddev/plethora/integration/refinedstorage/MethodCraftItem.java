package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingManager;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTaskError;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.integration.vanilla.NullableItemStack;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

@IMethod.Inject(value = NullableItemStack.class, modId = RS.ID)
public final class MethodCraftItem extends BasicMethod<NullableItemStack> {
	public MethodCraftItem() {
		super("craft", "(count:int):boolean, table -- Craft this item, returning if the item could be crafted and a " +
			"reference to the crafting task.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<NullableItemStack> context) {
		return super.canApply(context) && context.hasContext(INetworkNode.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<NullableItemStack> context, @Nonnull Object[] args) throws LuaException {
		final int quantity = getInt(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<NullableItemStack> baked = context.bake();

			ItemStack stack = baked.getTarget().getFilledStack();
			INetwork network = baked.getContext(INetworkNode.class).getNetwork();
			if (network == null) throw new LuaException("Cannot find network");

			ICraftingManager manager = network.getCraftingManager();

			ICraftingTask task = manager.create(stack, quantity);
			if (task == null) throw new LuaException("No matching patterns");

			ICraftingTaskError error = task.calculate();
			if (error != null) {
				String errorMessage;
				switch (error.getType()) {
					case RECURSIVE:
						errorMessage = "Encountered a recursive pattern";
						break;
					case TOO_COMPLEX:
						errorMessage = "Recipe is too complex";
						break;
					default:
						errorMessage = null;
						break;
				}
				return MethodResult.result(false, errorMessage);
			} else if (task.hasMissing()) {
				return MethodResult.result(false, "Missing requirements");
			} else {
				manager.add(task);
				return MethodResult.result(true, baked.makeChildId(task).getObject());
			}
		});
	}
}
