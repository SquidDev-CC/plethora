package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A meta provider for consumables: food and potions.
 *
 * Provides food and saturation for foodstuffs, and potion details for potions.
 */
@MetaProvider(ItemStack.class)
public class MetaItemConsumable implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemFood) {
			HashMap<Object, Object> data = Maps.newHashMap();
			ItemFood food = (ItemFood) item;
			data.put("heal", food.getHealAmount(stack));
			data.put("saturation", food.getSaturationModifier(stack));

			return data;
		} else if (item instanceof ItemPotion) {
			HashMap<Object, Object> data = Maps.newHashMap();
			ItemPotion itemPotion = (ItemPotion) item;

			data.put("splash", ItemPotion.isSplash(stack.getItemDamage()));

			Map<Integer, Map<String, Object>> effectsInfo = Maps.newHashMap();

			@SuppressWarnings("unchecked")
			List<PotionEffect> effects = itemPotion.getEffects(stack);
			if (effects != null && effects.size() > 0) {
				int i = 0;
				for (PotionEffect effect : effects) {
					Map<String, Object> entry = Maps.newHashMap();

					entry.put("duration", effect.getDuration() / 20); // ticks!
					entry.put("amplifier", effect.getAmplifier());
					int potionId = effect.getPotionID();
					if (potionId >= 0 && potionId < Potion.potionTypes.length) {

						Potion potion = Potion.potionTypes[potionId];
						data.put("name", potion.getName());
						data.put("instant", potion.isInstant());
						data.put("color", potion.getLiquidColor());
					}

					effectsInfo.put(i, entry);
					i++;
				}

				data.put("effects", effectsInfo);
			}

			return data;
		} else {
			return Collections.emptyMap();
		}
	}
}
