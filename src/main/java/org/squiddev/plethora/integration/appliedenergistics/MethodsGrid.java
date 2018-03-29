package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.integration.ItemFingerprint;

import java.util.Map;

import static org.squiddev.plethora.integration.appliedenergistics.MetaAEItemStack.getBasicProperties;

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
		IGrid grid = context.getTarget();
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IItemList<IAEItemStack> items = storageGrid.getInventory(channel).getStorageList();

		int i = 0;
		Map<Integer, Map<Object, Object>> output = Maps.newHashMapWithExpectedSize(items.size());
		for (IAEItemStack stack : items) output.put(++i, getBasicProperties(stack));
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
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IGrid> baked = context.bake();
			IAEItemStack stack = findStack(baked.getTarget(), fingerprint);

			return stack == null
				? MethodResult.empty()
				: MethodResult.result(baked.makeChildId(stack).getObject());
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
		final ItemFingerprint fingerprint = ItemFingerprint.fromLua(args, 0);

		return MethodResult.nextTick(() -> {
			IContext<IGrid> baked = context.bake();

			IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IStorageGrid grid = baked.getTarget().getCache(IStorageGrid.class);

			int i = 0;
			Map<Integer, Object> out = Maps.newHashMap();
			for (IAEItemStack aeStack : grid.getInventory(channel).getStorageList()) {
				if (fingerprint.matches(aeStack.getDefinition())) {
					out.put(++i, baked.makeChildId(aeStack).getObject());
				}
			}

			return MethodResult.result(out);
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
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageGrid grid = network.getCache(IStorageGrid.class);

		for (IAEItemStack aeStack : grid.getInventory(channel).getStorageList()) {
			if (fingerprint.matches(aeStack.getDefinition())) return aeStack;
		}

		return null;
	}
}
