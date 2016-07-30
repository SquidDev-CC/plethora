package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.reference.ItemSlot;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.HashMap;

public final class MethodsInventory {
	// TODO: Switch to new argument validation

	@Method(IItemHandler.class)
	public static class ListMethod extends BasicObjectMethod<IItemHandler> {
		public ListMethod() {
			super("list", true, "function():table -- List all items in this inventory");
		}

		@Override
		public Object[] apply(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
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
	}

	@Method(IItemHandler.class)
	public static class GetItemMethod extends BasicObjectMethod<IItemHandler> {
		public GetItemMethod() {
			super("getItem", true, "function(slot:integer):table|nil -- Get the item in a slot. The slot number starts from 1.");
		}

		@Override
		public Object[] apply(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
			if (args.length < 1 || !(args[0] instanceof Number)) throw new LuaException("Expected number");

			IItemHandler inventory = context.getTarget();
			int slot = ((Number) args[0]).intValue();
			if (slot < 1 || slot > inventory.getSlots()) throw new LuaException("Slot out of range");

			ItemStack stack = inventory.getStackInSlot(slot - 1);
			if (stack == null) {
				return null;
			} else {
				ItemSlot item = new ItemSlot(inventory, slot - 1);
				return new Object[]{context.makeChild(item).getObject()};
			}
		}
	}

	@Method(IItemHandler.class)
	public static class SizeMethod extends BasicObjectMethod<IItemHandler> {
		public SizeMethod() {
			super("size", true, "function():integer -- Get the size of the inventory");
		}

		@Override
		public Object[] apply(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
			return new Object[]{context.getTarget().getSlots()};
		}
	}

	@Method(IItemHandler.class)
	public static class MetadataMethod extends BasicObjectMethod<IItemHandler> {
		public MetadataMethod() {
			super("getMetadata", true, "function(slot:integer):table|nil -- Get the metadata of the item in a slot. The slot number starts from 1.");
		}

		@Override
		public Object[] apply(@Nonnull IContext<IItemHandler> context, @Nonnull Object[] args) throws LuaException {
			if (args.length < 1 || !(args[0] instanceof Number)) throw new LuaException("Expected number");

			IItemHandler inventory = context.getTarget();
			int slot = ((Number) args[0]).intValue();
			if (slot < 1 || slot > inventory.getSlots()) throw new LuaException("Slot out of range");

			ItemStack stack = inventory.getStackInSlot(slot - 1);
			if (stack == null) {
				return null;
			} else {
				return new Object[]{PlethoraAPI.instance().metaRegistry().getMeta(stack)};
			}
		}
	}
}
