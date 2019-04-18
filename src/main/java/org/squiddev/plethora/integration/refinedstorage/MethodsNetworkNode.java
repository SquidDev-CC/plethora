package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.ItemFingerprint;
import org.squiddev.plethora.integration.vanilla.NullableItemStack;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import java.util.*;

/**
 * Whilst these methods could be implemented on {@link INetwork}, we have to implement them on
 * {@link INetworkNode} instead as the converter may fail.
 */
public final class MethodsNetworkNode {
	private MethodsNetworkNode() {
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the energy usage of this RefinedStorage node")
	public static int getNodeEnergyUsage(@FromTarget INetworkNode node) {
		return node.getEnergyUsage();
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the energy usage of this RefinedStorage network")
	public static int getNetworkEnergyUsage(@FromTarget INetworkNode node) {
		INetwork network = node.getNetwork();
		return network != null ? network.getEnergyUsage() : node.getEnergyUsage();
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the energy stored usage in this RefinedStorage network")
	public static int getNetworkEnergyStored(@FromTarget INetworkNode node) {
		INetwork network = node.getNetwork();
		return network == null ? 0 : network.getEnergy().getStored();
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- List all items which are stored in the network")
	public static Map<Integer, ?> listAvailableItems(IContext<INetworkNode> context) {
		INetwork network = context.getTarget().getNetwork();
		if (network == null) return Collections.emptyMap();

		Collection<ItemStack> items = network.getItemStorageCache().getList().getStacks();
		HashMap<ItemIdentity, Map<String, Object>> seen = new HashMap<>();
		Map<Integer, Map<String, Object>> output = new HashMap<>(items.size());

		int i = 0;
		for (ItemStack stack : items) {
			Map<String, Object> basic = MetaItemBasic.getBasicMeta(stack);
			seen.put(new ItemIdentity(stack), basic);
			output.put(++i, basic);
		}

		for (ICraftingPattern pattern : network.getCraftingManager().getPatterns()) {
			for (ItemStack stack : pattern.getOutputs()) {
				if (stack == null || stack.isEmpty()) continue;
				ItemIdentity key = new ItemIdentity(stack);
				Map<String, Object> basic = seen.get(key);
				if (basic == null) {
					basic = MetaItemBasic.getBasicMeta(stack);
					basic.put("count", 0);
					output.put(++i, basic);
				}

				basic.put("isCraftable", true);
			}
		}

		return output;
	}

	@PlethoraMethod(
		modId = RS.ID,
		doc = "-- Search for an item in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static TypedLuaObject<NullableItemStack> findItem(IContext<INetworkNode> context, ItemFingerprint item) {
		INetwork network = context.getTarget().getNetwork();
		if (network == null) return null;

		for (ItemStack stack : network.getItemStorageCache().getList().getStacks()) {
			if (item.matches(stack)) {
				return context.makeChildId(NullableItemStack.normal(stack)).getObject();
			}
		}

		for (ICraftingPattern pattern : network.getCraftingManager().getPatterns()) {
			for (ItemStack stack : pattern.getOutputs()) {
				if (item.matches(stack)) {
					return context.makeChildId(NullableItemStack.empty(stack)).getObject();
				}
			}
		}

		return null;
	}

	@PlethoraMethod(
		modId = RS.ID,
		doc = "-- Search all items in the network. " +
			"You can specify the item as a string, with or without the damage value ('minecraft:stone' or 'minecraft:stone@0') " +
			"or as a table with 'name', 'damage' and 'nbthash' fields. You must specify the 'name', but you can " +
			"leave the other fields empty."
	)
	public static Map<Integer, TypedLuaObject<NullableItemStack>> findItems(final IContext<INetworkNode> context, ItemFingerprint item) {
		INetwork network = context.getTarget().getNetwork();
		if (network == null) return Collections.emptyMap();

		Set<ItemIdentity> seen = new HashSet<>();

		int i = 0;
		Map<Integer, TypedLuaObject<NullableItemStack>> out = new HashMap<>();
		for (ItemStack stack : network.getItemStorageCache().getList().getStacks()) {
			if (item.matches(stack)) {
				seen.add(new ItemIdentity(stack));
				out.put(++i, context.makeChildId(NullableItemStack.normal(stack)).getObject());
			}
		}

		for (ICraftingPattern pattern : network.getCraftingManager().getPatterns()) {
			for (ItemStack stack : pattern.getOutputs()) {
				if (stack != null && item.matches(stack) && seen.add(new ItemIdentity(stack))) {
					out.put(++i, context.makeChildId(NullableItemStack.empty(stack)).getObject());
				}
			}
		}

		return out;
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- List all crafting tasks in the network")
	public static Map<Integer, Object> getCraftingTasks(IContext<INetworkNode> context) {
		INetwork network = context.getTarget().getNetwork();
		if (network == null) return Collections.emptyMap();

		Collection<ICraftingTask> tasks = network.getCraftingManager().getTasks();

		int i = 0;
		Map<Integer, Object> output = new HashMap<>(tasks.size());
		for (ICraftingTask task : tasks) {
			output.put(++i, context.makeChildId(task).getObject());
		}

		return output;
	}
}
