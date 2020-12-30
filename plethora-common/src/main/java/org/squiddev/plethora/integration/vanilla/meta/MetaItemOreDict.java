package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Ore Dictionary lookup for items
 */
@Injects
public final class MetaItemOreDict extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return Collections.emptyMap();

		int[] oreIds = OreDictionary.getOreIDs(stack);
		if (oreIds.length <= 0) return Collections.emptyMap();

		HashMap<String, Boolean> list = new HashMap<>(oreIds.length);
		for (int id : oreIds) list.put(OreDictionary.getOreName(id), true);
		return Collections.singletonMap("ores", list);
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return new ItemStack(Blocks.GLASS);
	}
}
