package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingCPU {
	/**
	 * {@link CraftingCPUCluster.finalOutput} contains the requested output of the crafting job, but it is not
	 * public. We thus use reflection to access the field.
	 */
	private static final TypedField<CraftingCPUCluster, IAEItemStack> OUTPUT = TypedField.of(CraftingCPUCluster.class, "finalOutput");

	private static final CraftingItemList[] CRAFTING_ITEM_LIST = {
		CraftingItemList.ACTIVE,
		CraftingItemList.PENDING,
		CraftingItemList.STORAGE
	};

	private static List<Map<String, ?>> getJobItemList(CraftingCPUCluster cluster, CraftingItemList whichList) {

		IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
		cluster.getListOfItem(list, whichList);

		List<Map<String, ?>> listData = new ArrayList<>(list.size());

		for (IAEItemStack stack : list) {
			listData.add(MetaAppliedEnergistics.getItemStackProperties(stack));
		}

		return listData;
	}

	@Nullable
	public static Map<String, Object> getCurrentJob(ICraftingCPU cpu) {
		if (!(cpu instanceof CraftingCPUCluster)) return null;
		CraftingCPUCluster cluster = (CraftingCPUCluster) cpu;

		Map<String, Object> jobData = new HashMap<>(CRAFTING_ITEM_LIST.length);
		for (CraftingItemList whichList : CRAFTING_ITEM_LIST) {
			jobData.put(whichList.name().toLowerCase(), getJobItemList(cluster, whichList));
		}

		IAEItemStack finalOutput = OUTPUT.get(cluster);
		if (finalOutput != null) {
			jobData.put("finalOutput", MetaAppliedEnergistics.getItemStackProperties(finalOutput));
		}

		return jobData;
	}
}
