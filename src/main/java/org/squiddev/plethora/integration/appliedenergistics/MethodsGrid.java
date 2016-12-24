package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.integration.ItemFingerprint;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.reference.Reference.id;

public class MethodsGrid {
	@BasicObjectMethod.Inject(
		value = IGridBlock.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():int -- Get the energy usage of this AE node"
	)
	public static Object[] getNodeEnergyUsage(IContext<IGridBlock> context, Object[] args) {
		return new Object[]{context.getTarget().getIdlePowerUsage()};
	}

	@BasicObjectMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():int -- Get the energy usage of this AE network"
	)
	public static Object[] getNetworkEnergyUsage(IContext<IGrid> context, Object[] args) {
		IEnergyGrid energy = context.getTarget().getCache(IEnergyGrid.class);
		return new Object[]{energy.getAvgPowerUsage()};
	}

	@BasicObjectMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():int -- Get the energy stored usage in this AE network"
	)
	public static Object[] getNetworkEnergyStored(IContext<IGrid> context, Object[] args) {
		IEnergyGrid energy = context.getTarget().getCache(IEnergyGrid.class);
		return new Object[]{energy.getStoredPower()};
	}

	@BasicObjectMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():table -- List all items which are stored in the network"
	)
	public static Object[] listAvailableItems(IContext<IGrid> context, Object[] args) {
		IStorageGrid grid = context.getTarget().getCache(IStorageGrid.class);
		IItemList<IAEItemStack> items = grid.getItemInventory().getStorageList();

		int i = 0;
		Map<Integer, Map<Object, Object>> output = Maps.newHashMapWithExpectedSize(items.size());
		for (IAEItemStack stack : items) {
			output.put(++i, MetaItemBasic.getBasicProperties(stack.getItemStack()));
		}
		return new Object[]{output};
	}

	@BasicMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID,
		doc = "function(item:string|table):table -- Search for an item in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static MethodResult findItem(final IUnbakedContext<IGrid> context, Object[] args) throws LuaException {
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args.length == 0 ? null : args[0]);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IGrid> baked = context.bake();
				IAEItemStack stack = findStack(baked.getTarget(), fingerprint);

				return stack == null
					? MethodResult.empty()
					: MethodResult.result(baked.makeChild(id(stack)).getObject());
			}
		});
	}

	@BasicMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID,
		doc = "function(item:string|table):table -- Search all items in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static MethodResult findItems(final IUnbakedContext<IGrid> context, Object[] args) throws LuaException {
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args.length == 0 ? null : args[0]);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IGrid> baked = context.bake();

				int i = 0;
				Map<Integer, Object> out = Maps.newHashMap();
				IStorageGrid grid = baked.getTarget().getCache(IStorageGrid.class);
				for (IAEItemStack aeStack : grid.getItemInventory().getStorageList()) {
					ItemStack stack = aeStack.getItemStack();
					if (fingerprint.matches(stack)) {
						out.put(++i, baked.makeChild(id(aeStack)).getObject());
					}
				}

				return MethodResult.result(out);
			}
		});
	}

	@BasicObjectMethod.Inject(
		value = IGrid.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():table -- List all crafting cpus in the network"
	)
	public static Object[] getCraftingCPUs(IContext<IGrid> context, Object[] args) {
		ICraftingGrid crafting = context.getTarget().getCache(ICraftingGrid.class);

		return new Object[]{ContextHelpers.getMetaList(context, crafting.getCpus())};
	}

	private static IAEItemStack findStack(IGrid network, ItemFingerprint fingerprint) {
		IStorageGrid grid = network.getCache(IStorageGrid.class);
		for (IAEItemStack aeStack : grid.getItemInventory().getStorageList()) {
			ItemStack stack = aeStack.getItemStack();
			if (fingerprint.matches(stack)) return aeStack;
		}

		return null;
	}
}
