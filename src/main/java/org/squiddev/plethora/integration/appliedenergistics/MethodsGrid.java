package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import net.minecraftforge.fluids.FluidRegistry;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.ItemFingerprint;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class MethodsGrid {
	private MethodsGrid() {
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "-- Get the energy usage of this AE node")
	public static double getNodeEnergyUsage(@FromTarget IGridBlock grid) {
		return grid.getIdlePowerUsage();
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "-- Get the energy usage of this AE network")
	public static double getNetworkEnergyUsage(@FromTarget IGrid grid) {
		IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
		return energy.getAvgPowerUsage();
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "function():int -- Get the energy stored usage in this AE network")
	public static double getNetworkEnergyStored(@FromTarget IGrid grid) {
		IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
		return energy.getStoredPower();
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "-- List all items which are stored in the network")
	public static List<Map<String, ?>> listAvailableItems(IContext<IGrid> context) {
		IGrid grid = context.getTarget();
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IItemList<IAEItemStack> items = storageGrid.getInventory(channel).getStorageList();

		return Helpers.map(items, items.size(), MetaAppliedEnergistics::getItemStackProperties);
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "-- List all fluids which are stored in the network")
	public static List<Map<String, ?>> listAvailableFluids(IContext<IGrid> context) {
		IGrid grid = context.getTarget();
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		IFluidStorageChannel channel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
		IItemList<IAEFluidStack> items = storageGrid.getInventory(channel).getStorageList();

		return Helpers.map(items, items.size(), MetaAppliedEnergistics::getFluidStackProperties);
	}

	@Optional
	@PlethoraMethod(
			modId = AppEng.MOD_ID,
			doc = "function(item:string):table -- Search for a fluid in the network. " +
					"You can specify the fluid as a string ('minecraft:water')."
	)
	public static TypedLuaObject<IAEFluidStack> findFluid(final IContext<IGrid> baked, String fluidName) {
		IAEFluidStack stack = findFluid(baked.getTarget(), fluidName);
		return stack == null ? null : baked.makeChildId(stack).getObject();
	}

	@Optional
	@PlethoraMethod(
		modId = AppEng.MOD_ID,
		doc = "function(item:string|table):table -- Search for an item in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static TypedLuaObject<IAEItemStack> findItem(final IContext<IGrid> baked, ItemFingerprint fingerprint) {
		IAEItemStack stack = findStack(baked.getTarget(), fingerprint);
		return stack == null ? null : baked.makeChildId(stack).getObject();
	}

	@PlethoraMethod(
		modId = AppEng.MOD_ID,
		doc = "-- Search all items in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static List<TypedLuaObject<IAEItemStack>> findItems(IContext<IGrid> context, ItemFingerprint item) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageGrid grid = context.getTarget().getCache(IStorageGrid.class);

		return StreamSupport.stream(grid.getInventory(channel).getStorageList().spliterator(), false)
			.filter(x -> item.matches(x.getDefinition()))
			.map(x -> context.makeChildId(x).getObject())
			.collect(Collectors.toList());
	}

	@PlethoraMethod(modId = AppEng.MOD_ID, doc = "-- List all crafting cpus in the network")
	public static List<TypedMeta<ICraftingCPU, ?>> getCraftingCPUs(IContext<IGrid> context) {
		ICraftingGrid crafting = context.getTarget().getCache(ICraftingGrid.class);
		return ContextHelpers.getMetaList(context, crafting.getCpus());
	}

	private static IAEItemStack findStack(IGrid network, ItemFingerprint fingerprint) {
		IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		IStorageGrid grid = network.getCache(IStorageGrid.class);

		for (IAEItemStack aeStack : grid.getInventory(channel).getStorageList()) {
			if (fingerprint.matches(aeStack.getDefinition())) return aeStack;
		}

		return null;
	}

	private static IAEFluidStack findFluid(IGrid network, String fluidName) {
		IFluidStorageChannel channel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
		IStorageGrid grid = network.getCache(IStorageGrid.class);

		for (IAEFluidStack aeStack : grid.getInventory(channel).getStorageList()) {
			if (FluidRegistry.getDefaultFluidName(aeStack.getFluid()).equals(fluidName)) return aeStack;
		}

		return null;
	}
}
