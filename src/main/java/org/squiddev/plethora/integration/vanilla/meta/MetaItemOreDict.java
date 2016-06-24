package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Ore Dictionary lookup for items
 */
@MetaProvider(value = ItemStack.class, namespace = "ores")
public class MetaItemOreDict implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		int[] oreIds = OreDictionary.getOreIDs(stack);
		if (oreIds.length > 0) {
			HashMap<Object, Object> list = Maps.newHashMap();

			for (int id : oreIds) {
				list.put(OreDictionary.getOreName(id), true);
			}

			return list;
		} else {
			return Collections.emptyMap();
		}
	}
}
