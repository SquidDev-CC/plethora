package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.api.reference.ItemSlot;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

public final class MethodsInventory {
	@PlethoraMethod(doc = "-- List all items in this inventory")
	public static Map<Integer, Object> list(@Nonnull @FromTarget IItemHandler inventory) {
		HashMap<Integer, Object> items = Maps.newHashMap();
		int size = inventory.getSlots();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				items.put(i + 1, MetaItemBasic.getBasicMeta(stack));
			}
		}

		return items;
	}

	@Nullable
	@PlethoraMethod(doc = "-- The item in the specified slot. The slot number starts from 1.")
	public static ILuaObject getItem(IContext<IItemHandler> baked, int slot) throws LuaException {
		IItemHandler inventory = baked.getTarget();

		assertBetween(slot, 1, inventory.getSlots(), "Slot out of range (%s)");

		ItemStack stack = inventory.getStackInSlot(slot - 1);
		return stack.isEmpty() ? null : baked.makeChildId(new ItemSlot(inventory, slot - 1)).getObject();
	}

	@PlethoraMethod(doc = "-- The size of the inventory")
	public static int size(@FromTarget IItemHandler inventory) {
		return inventory.getSlots();
	}

	@Nullable
	@PlethoraMethod(doc = "-- The metadata of the item in the specified slot. The slot number starts from 1.")
	public static Map<Object, Object> getItemMeta(IContext<IItemHandler> context, int slot) throws LuaException {
		IItemHandler inventory = context.getTarget();
		assertBetween(slot, 1, inventory.getSlots(), "Slot out of range (%s)");

		ItemStack stack = inventory.getStackInSlot(slot - 1);
		return stack.isEmpty() ? null : context.makePartialChild(stack).getMeta();
	}
}
