package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists enchantments on items
 */
@MetaProvider(value = ItemStack.class, namespace = "enchantements")
public class MetaItemEnchantment implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		NBTTagList enchants = null;
		if (stack.isItemEnchanted()) {
			enchants = stack.getEnchantmentTagList();
		} else if (stack.getItem() instanceof ItemEnchantedBook) {
			enchants = ((ItemEnchantedBook) stack.getItem()).getEnchantments(stack);
		}

		if (enchants != null && enchants.tagCount() > 0) {
			HashMap<Object, Object> items = Maps.newHashMap();

			for (int i = 0; i < enchants.tagCount(); i++) {
				NBTTagCompound tag = enchants.getCompoundTagAt(i);
				Enchantment enchantement = Enchantment.getEnchantmentById(tag.getShort("id"));
				if (enchantement != null) {
					HashMap<String, Object> enchant = Maps.newHashMap();
					enchant.put("name", enchantement.getName());

					short level = tag.getShort("lvl");
					enchant.put("level", level);
					enchant.put("fullName", enchantement.getTranslatedName(level));

					items.put(i + 1, enchant);
				}
			}

			return items;
		} else {
			return Collections.emptyMap();
		}
	}
}
