package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists enchantments on items
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "enchantments")
public class MetaItemEnchantment extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

		if (!enchants.isEmpty()) {
			HashMap<Object, Object> items = new HashMap<>();

			int i = 0;
			for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
				Enchantment enchantment = entry.getKey();
				HashMap<String, Object> enchant = new HashMap<>();
				enchant.put("name", enchantment.getName());

				int level = entry.getValue();
				enchant.put("level", entry.getValue());
				enchant.put("fullName", enchantment.getTranslatedName(level));

				items.put(++i, enchant);
			}

			return items;
		} else {
			return Collections.emptyMap();
		}
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		ItemStack stack = new ItemStack(Items.DIAMOND_HOE);
		EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.UNBREAKING, 5), stack);
		return stack;
	}
}
