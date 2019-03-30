package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A meta provider for consumables: food and potions.
 *
 * Provides food and saturation for foodstuffs, and potion details for potions.
 */
@Injects
public final class MetaItemConsumable extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemFood) {
			HashMap<Object, Object> data = new HashMap<>();
			ItemFood food = (ItemFood) item;
			data.put("heal", food.getHealAmount(stack));
			data.put("saturation", food.getSaturationModifier(stack));

			return data;
		} else if (item instanceof ItemPotion) {
			HashMap<Object, Object> data = new HashMap<>();
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

			Map<Integer, Map<String, Object>> effectsInfo = new HashMap<>();

			PotionType effects = PotionUtils.getPotionFromItem(stack);
			if (!effects.getEffects().isEmpty()) {
				int i = 0;
				for (PotionEffect effect : effects.getEffects()) {
					Map<String, Object> entry = new HashMap<>();

					entry.put("duration", effect.getDuration() / 20); // ticks!
					entry.put("amplifier", effect.getAmplifier());

					Potion potion = effect.getPotion();
					entry.put("name", potion.getName());
					entry.put("instant", potion.isInstant());
					entry.put("color", potion.getLiquidColor());

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

	@Nullable
	@Override
	public ItemStack getExample() {
		// TODO: Split this provider into potions and food
		return new ItemStack(Items.COOKED_BEEF);
	}
}
