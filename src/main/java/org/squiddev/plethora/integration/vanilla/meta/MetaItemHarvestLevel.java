package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays classes for tools
 */
@MetaProvider(value = ItemStack.class, namespace = "toolClass")
public class MetaItemHarvestLevel implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		Item item = stack.getItem();
		if (!item.getToolClasses(stack).isEmpty()) {
			HashMap<Object, Object> types = Maps.newHashMap();

			for (String tool : item.getToolClasses(stack)) {
				types.put(tool, item.getHarvestLevel(stack, tool));
			}

			return types;
		} else {
			return Collections.emptyMap();
		}
	}
}
