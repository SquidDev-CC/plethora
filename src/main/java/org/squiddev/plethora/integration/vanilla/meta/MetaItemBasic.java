package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds basic properties for item stacks.
 */
@MetaProvider(ItemStack.class)
public class MetaItemBasic implements IMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		HashMap<Object, Object> data = getBasicProperties(stack);

		String display = stack.getDisplayName();
		data.put("displayName", display == null || display.length() == 0 ? stack.getUnlocalizedName() : display);
		data.put("rawName", stack.getUnlocalizedName());

		data.put("maxCount", stack.getMaxStackSize());
		data.put("maxDamage", stack.getMaxDamage());

		if (stack.getItem().showDurabilityBar(stack)) {
			data.put("durability", stack.getItem().getDurabilityForDisplay(stack));
		}

		return data;
	}

	@Nonnull
	public static HashMap<Object, Object> getBasicProperties(@Nonnull ItemStack stack) {
		HashMap<Object, Object> data = Maps.newHashMap();

		data.put("name", Item.itemRegistry.getNameForObject(stack.getItem()).toString());
		data.put("damage", stack.getItemDamage());
		data.put("count", stack.stackSize);

		return data;
	}
}
