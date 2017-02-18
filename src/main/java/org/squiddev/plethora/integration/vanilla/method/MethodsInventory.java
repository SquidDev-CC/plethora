package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.ItemSlot;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getInt;

public final class MethodsInventory {
	@BasicObjectMethod.Inject(
		value = IItemHandler.class, worldThread = true,
		doc = "function():table -- List all items in this inventory"
	)
	public static Object[] list(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
		HashMap<Integer, Object> items = Maps.newHashMap();
		IItemHandler inventory = context.getTarget();
		int size = inventory.getSlots();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				items.put(i + 1, MetaItemBasic.getBasicProperties(stack));
			}
		}

		return new Object[]{items};
	}

	@BasicMethod.Inject(
		value = IItemHandler.class,
		doc = "function(slot:integer):table|nil -- The item in the specified slot. The slot number starts from 1."
	)
	public static MethodResult getItem(final @Nonnull IUnbakedContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
		final int slot = getInt(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IItemHandler inventory = context.bake().getTarget();

				assertBetween(slot, 1, inventory.getSlots(), "Slot out of range (%s)");

				ItemStack stack = inventory.getStackInSlot(slot - 1);
				if (stack == null) {
					return MethodResult.empty();
				} else {
					ItemSlot item = new ItemSlot(inventory, slot - 1);
					return MethodResult.result(context.makeChild(item).getObject());
				}
			}
		});
	}

	@BasicObjectMethod.Inject(
		value = IItemHandler.class, worldThread = false,
		doc = "function():integer -- The size of the inventory"
	)
	public static Object[] size(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
		return new Object[]{context.getTarget().getSlots()};
	}

	@BasicMethod.Inject(
		value = IItemHandler.class,
		doc = "function(slot:integer):table|nil -- The metadata of the item in the specified slot. The slot number starts from 1."
	)
	public static MethodResult getItemMeta(final @Nonnull IUnbakedContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
		final int slot = getInt(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IItemHandler> baked = context.bake();
				IItemHandler inventory = baked.getTarget();

				assertBetween(slot, 1, inventory.getSlots(), "Slot out of range (%s)");

				ItemStack stack = inventory.getStackInSlot(slot - 1);
				if (stack == null) {
					return MethodResult.empty();
				} else {
					return MethodResult.result(baked.makePartialChild(stack).getMeta());
				}
			}
		});
	}
}
