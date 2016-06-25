package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta provider for amour properties. Material is handled in {@link MetaItemMaterial}.
 */
@MetaProvider(ItemStack.class)
public class MetaItemArmor implements IMetaProvider<ItemStack> {
	private static String convertArmorType(int armorType) {
		switch (armorType) {
			case 0:
				return "helmet";
			case 1:
				return "plate";
			case 2:
				return "legs";
			case 3:
				return "boots";
			default:
				return "unknown";
		}
	}

	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemArmor) {
			ItemArmor armor = (ItemArmor) item;
			HashMap<Object, Object> data = Maps.newHashMap();
			data.put("armorType", convertArmorType(armor.armorType));

			int color = armor.getColor(stack);
			if (color >= 0) data.put("color", color);

			return data;
		} else {
			return Collections.emptyMap();
		}
	}
}
