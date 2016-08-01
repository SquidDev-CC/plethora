package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Ore Dictionary lookup for items
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "ores")
public class MetaItemOreDict extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
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
