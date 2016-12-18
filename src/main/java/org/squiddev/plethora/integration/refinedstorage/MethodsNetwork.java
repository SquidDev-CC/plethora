package org.squiddev.plethora.integration.refinedstorage;

import com.google.common.collect.Maps;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.integration.ItemFingerprint;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.reference.Reference.id;

public class MethodsNetwork {
	@BasicObjectMethod.Inject(
		value = INetworkNode.class, modId = RS.ID, worldThread = true,
		doc = "function():int -- Get the energy usage of this RefinedStorage node"
	)
	public static Object[] getNodeEnergyUsage(IContext<INetworkNode> context, Object[] args) {
		return new Object[]{context.getTarget().getEnergyUsage()};
	}

	@BasicObjectMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID, worldThread = true,
		doc = "function():int -- Get the energy usage of this RefinedStorage network"
	)
	public static Object[] getNetworkEnergyUsage(IContext<INetworkMaster> context, Object[] args) {
		return new Object[]{context.getTarget().getEnergyUsage()};
	}

	@BasicObjectMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID, worldThread = true,
		doc = "function():int -- Get the energy stored usage in this RefinedStorage network"
	)
	public static Object[] getNetworkEnergyStored(IContext<INetworkMaster> context, Object[] args) {
		return new Object[]{context.getTarget().getEnergy().getEnergyStored()};
	}

	@BasicObjectMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID, worldThread = true,
		doc = "function():table -- List all items which are stored in the network"
	)
	public static Object[] listAvailableItems(IContext<INetworkMaster> context, Object[] args) {
		Collection<ItemStack> items = context.getTarget().getItemStorageCache().getList().getStacks();

		int i = 0;
		Map<Integer, Map<Object, Object>> output = Maps.newHashMapWithExpectedSize(items.size());
		for (ItemStack stack : items) {
			output.put(++i, MetaItemBasic.getBasicProperties(stack));
		}
		return new Object[]{output};
	}

	@BasicMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID,
		doc = "function(item:string|table):table -- Search for an item in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static MethodResult findItem(final IUnbakedContext<INetworkMaster> context, Object[] args) throws LuaException {
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args.length == 0 ? null : args[0]);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<INetworkMaster> baked = context.bake();
				ItemStack stack = findStack(baked.getTarget(), fingerprint);

				return stack == null
					? MethodResult.empty()
					: MethodResult.result(baked.makeChild(id(stack)).getObject());
			}
		});
	}

	@BasicMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID,
		doc = "function(item:string|table):table -- Search all items in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static MethodResult findItems(final IUnbakedContext<INetworkMaster> context, Object[] args) throws LuaException {
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args.length == 0 ? null : args[0]);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<INetworkMaster> baked = context.bake();

				int i = 0;
				Map<Integer, Object> out = Maps.newHashMap();
				for (ItemStack stack : baked.getTarget().getItemStorageCache().getList().getStacks()) {
					if (fingerprint.matches(stack)) {
						out.put(++i, baked.makeChild(id(stack)).getObject());
					}
				}

				return MethodResult.result(out);
			}
		});
	}

	@BasicObjectMethod.Inject(
		value = INetworkMaster.class, modId = RS.ID, worldThread = true,
		doc = "function():table -- List all crafting tasks in the network"
	)
	public static Object[] getCraftingTasks(IContext<INetworkMaster> context, Object[] args) {
		List<ICraftingTask> tasks = context.getTarget().getCraftingTasks();

		int i = 0;
		Map<Integer, Object> output = Maps.newHashMapWithExpectedSize(tasks.size());
		for (ICraftingTask task : tasks) {
			output.put(++i, context.makeChild(Reference.id(task)).getObject());
		}

		return new Object[]{output};
	}

	private static ItemStack findStack(INetworkMaster network, ItemFingerprint fingerprint) {
		for (ItemStack stack : network.getItemStorageCache().getList().getStacks()) {
			if (fingerprint.matches(stack)) return stack;
		}

		return null;
	}
}
