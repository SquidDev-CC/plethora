package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.item.*;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.Map;

/**
 * Displays tool's material
 */
@MetaProvider(ItemStack.class)
public class MetaItemMaterial implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		String name = getName(stack);
		if (name == null) return Collections.emptyMap();

		return Collections.<Object, Object>singletonMap("material", stack);
	}

	private static String getName(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemTool) {
			return ((ItemTool) item).getToolMaterialName();
		} else if (item instanceof ItemSword) {
			return ((ItemSword) item).getToolMaterialName();
		} else if (item instanceof ItemArmor) {
			return ((ItemArmor) item).getArmorMaterial().toString();
		} else {
			return null;
		}
	}
}
