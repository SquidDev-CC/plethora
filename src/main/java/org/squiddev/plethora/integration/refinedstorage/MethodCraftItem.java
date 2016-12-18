package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

@IMethod.Inject(value = ItemStack.class, modId = RS.ID)
public final class MethodCraftItem extends BasicMethod<ItemStack> {
	public MethodCraftItem() {
		super("craft", "(count:int):boolean, table -- Craft this item, returning if the item could be crafted and a " +
			"reference to the crafting task.");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
		return super.canApply(context) && context.hasContext(INetworkMaster.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
		final int quantity = ArgumentHelper.getInt(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<ItemStack> baked = context.bake();

				ItemStack stack = baked.getTarget();
				INetworkMaster network = baked.getContext(INetworkMaster.class);

				ICraftingPattern pattern = network.getPattern(stack);
				if (pattern == null) throw new LuaException("No matching patterns");

				ICraftingTask task = network.createCraftingTask(stack, pattern, quantity);
				task.calculate();

				boolean success = task.isValid() && task.getMissing().getStacks().isEmpty();
				if (success) {
					network.addCraftingTask(task);
				}

				return MethodResult.result(success, context.makeChild(Reference.id(task)).getObject());
			}
		});
	}
}
