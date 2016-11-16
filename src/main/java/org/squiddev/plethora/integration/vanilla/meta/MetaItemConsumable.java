package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A meta provider for consumables: food and potions.
 *
 * Provides food and saturation for foodstuffs, and potion details for potions.
 */
@IMetaProvider.Inject(ItemStack.class)
public class MetaItemConsumable extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
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

			if (itemPotion instanceof ItemSplashPotion) {
				data.put("splash", true);
				data.put("type", "splash");
			} else if (itemPotion instanceof ItemLingeringPotion) {
				data.put("splash", false);
				data.put("type", "lingering");
			} else {
				data.put("type", "normal");
			}

			Map<Integer, Map<String, Object>> effectsInfo = Maps.newHashMap();

			PotionType effects = PotionUtils.getPotionFromItem(stack);
			if (effects.getEffects().size() > 0) {
				int i = 0;
				for (PotionEffect effect : effects.getEffects()) {
					Map<String, Object> entry = Maps.newHashMap();

					entry.put("duration", effect.getDuration() / 20); // ticks!
					entry.put("amplifier", effect.getAmplifier());

					Potion potion = effect.getPotion();
					data.put("name", potion.getName());
					data.put("instant", potion.isInstant());
					data.put("color", potion.getLiquidColor());

					effectsInfo.put(i + 1, entry);
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
