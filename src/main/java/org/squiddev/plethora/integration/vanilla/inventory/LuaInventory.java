package org.squiddev.plethora.integration.vanilla.inventory;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.reference.ItemSlot;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import java.util.HashMap;

public final class LuaInventory {
	@Method(IInventory.class)
	public static class ListMethod extends BasicMethod<IInventory> {
		public ListMethod() {
			super("list", true);
		}

		@Override
		public Object[] apply(IContext<IInventory> context, Object[] args) throws LuaException {
			HashMap<Integer, Object> items = Maps.newHashMap();
			IInventory inventory = context.getTarget();
			int size = inventory.getSizeInventory();
			for (int i = 0; i < size; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack != null) {
					items.put(i + 1, MetaItemBasic.getBasicProperties(stack));
				}
			}

			return new Object[]{items};
		}
	}

	@Method(IInventory.class)
	public static class GetItemMethod extends BasicMethod<IInventory> {
		public GetItemMethod() {
			super("getItem", true);
		}

		@Override
		public Object[] apply(IContext<IInventory> context, Object[] args) throws LuaException {
			if (args.length < 1 || !(args[0] instanceof Number)) throw new LuaException("Expected number");

			IInventory inventory = context.getTarget();
			int slot = ((Number) args[0]).intValue();
			if (slot < 1 || slot > inventory.getSizeInventory()) throw new LuaException("Slot out of range");

			ItemStack stack = inventory.getStackInSlot(slot - 1);
			if (stack == null) {
				return new Object[]{false, "No item there"};
			} else {
				ItemSlot item = new ItemSlot(inventory, slot - 1);
				return new Object[]{PlethoraAPI.instance().methodRegistry().getObject(context.makeChild(item))};
			}
		}
	}

	@Method(IInventory.class)
	public static class SizeMethod extends BasicMethod<IInventory> {
		public SizeMethod() {
			super("size", true);
		}

		@Override
		public Object[] apply(IContext<IInventory> context, Object[] args) throws LuaException {
			return new Object[]{context.getTarget().getSizeInventory()};
		}
	}

	@Method(IInventory.class)
	public static class MetadataMethod extends BasicMethod<IInventory> {
		public MetadataMethod() {
			super("getMetadata", true);
		}

		@Override
		public Object[] apply(IContext<IInventory> context, Object[] args) throws LuaException {
			if (args.length < 1 || !(args[0] instanceof Number)) throw new LuaException("Expected number");

			IInventory inventory = context.getTarget();
			int slot = ((Number) args[0]).intValue();
			if (slot < 1 || slot > inventory.getSizeInventory()) throw new LuaException("Slot out of range");

			ItemStack stack = inventory.getStackInSlot(slot - 1);
			if (stack == null) {
				return new Object[]{false, "No item there"};
			} else {
				return new Object[]{PlethoraAPI.instance().metaRegistry().getMeta(stack)};
			}
		}
	}
}
