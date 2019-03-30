package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays classes for tools
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "toolClass")
public class MetaItemHarvestLevel extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (!item.getToolClasses(stack).isEmpty()) {
			HashMap<Object, Object> types = new HashMap<>();

			for (String tool : item.getToolClasses(stack)) {
				types.put(tool, item.getHarvestLevel(stack, tool, null, null));
			}

			return types;
		} else {
			return Collections.emptyMap();
		}
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return new ItemStack(Items.DIAMOND_PICKAXE);
	}
}
