package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta provider for amour properties. Material is handled in {@link MetaItemMaterial}.
 */
@IMetaProvider.Inject(ItemStack.class)
public class MetaItemArmor extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemArmor) {
			ItemArmor armor = (ItemArmor) item;
			HashMap<Object, Object> data = Maps.newHashMap();
			data.put("armorType", armor.armorType.getName());

			int color = armor.getColor(stack);
			if (color >= 0) data.put("color", color);

			return data;
		} else {
			return Collections.emptyMap();
		}
	}
}
