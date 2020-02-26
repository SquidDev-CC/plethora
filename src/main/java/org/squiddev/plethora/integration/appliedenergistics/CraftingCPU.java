package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingCPU {

	private static Field FINAL_OUTPUT_FIELD;
	static {
		try {
			FINAL_OUTPUT_FIELD = CraftingCPUCluster.class.getDeclaredField("finalOutput");
			FINAL_OUTPUT_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@link CraftingCPUCluster.finalOutput } contains the requested output of the crafting job, but it is not
	 * public. We thus use reflection to access the field.
	 *
	 * @param cluster the crafting cluster
	 * @return the cluster's current job's final output
	 */
	private static IAEItemStack getFinalOutput(CraftingCPUCluster cluster) {
		try {
			return (IAEItemStack) FINAL_OUTPUT_FIELD.get(cluster);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final CraftingItemList[] CRAFTING_ITEM_LIST = {
			CraftingItemList.ACTIVE,
			CraftingItemList.PENDING,
			CraftingItemList.STORAGE
	};

	private static List<Map<String, Object>> getJobItemList(CraftingCPUCluster cluster, CraftingItemList whichList) {

		IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
		cluster.getListOfItem(list, whichList);

		List<Map<String, Object>> listData = new ArrayList<>(list.size());

		for (IAEItemStack stack : list) {
			listData.add(MetaAppliedEnergistics.getItemStackProperties(stack));
		}

		return listData;
	}

	public static Map<String, Object> getCurrentJob(ICraftingCPU cpu) {

		if (!(cpu instanceof CraftingCPUCluster)) {
			return null;
		}
		CraftingCPUCluster cluster = (CraftingCPUCluster) cpu;

		Map<String, Object> jobData = new HashMap<>(CRAFTING_ITEM_LIST.length);

		for (CraftingItemList whichList : CRAFTING_ITEM_LIST) {
			jobData.put(whichList.name().toLowerCase(), getJobItemList(cluster, whichList));
		}

		IAEItemStack finalOutput = CraftingCPU.getFinalOutput(cluster);
		if (finalOutput != null) {
			jobData.put("finalOutput", MetaAppliedEnergistics.getItemStackProperties(finalOutput));
		}

		return jobData;
	}
}
