package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingManager;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternChain;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
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
		return super.canApply(context) && context.hasContext(INetwork.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<NullableItemStack> context, @Nonnull Object[] args) throws LuaException {
		final int quantity = getInt(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<NullableItemStack> baked = context.bake();

			ItemStack stack = baked.getTarget().getFilledStack();
			INetwork network = baked.getContext(INetwork.class);
			ICraftingManager manager = network.getCraftingManager();

			ICraftingPatternChain chain = manager.getPatternChain(stack);
			if (chain == null) throw new LuaException("No matching patterns");

			ICraftingTask task = manager.create(stack, chain, quantity, true);
			task.calculate();

			boolean success = task.isValid() && task.getMissing().getStacks().isEmpty();
			if (success) {
				manager.add(task);
			}

			return MethodResult.result(success, baked.makeChildId(task).getObject());
		});
	}
}
