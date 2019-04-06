package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.api.method.LuaList;

import javax.annotation.Nonnull;
import java.util.*;

@Injects
public final class MetaItems {
	public static final IMetaProvider<ItemStack> ITEM_FOOD = new ItemStackMetaProvider<ItemFood>(ItemFood.class,
		"Provides the hunger and saturation this foodstuff restores."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull ItemFood food) {
			HashMap<String, Object> data = new HashMap<>(2);
			data.put("heal", food.getHealAmount(stack));
			data.put("saturation", food.getSaturationModifier(stack));
			return data;
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return new ItemStack(Items.BAKED_POTATO);
		}
	};

	public static final IMetaProvider<ItemStack> ITEM_POTION = new ItemStackMetaProvider<ItemPotion>(ItemPotion.class,
		"Provides the potion type and effects of this item."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull ItemPotion itemPotion) {
			HashMap<String, Object> data = new HashMap<>(2);

			if (itemPotion instanceof ItemSplashPotion) {
				data.put("splash", true);
				data.put("potionType", "splash");
			} else if (itemPotion instanceof ItemLingeringPotion) {
				data.put("potionType", "lingering");
			} else {
				data.put("potionType", "normal");
			}

			PotionType potionType = PotionUtils.getPotionFromItem(stack);
			data.put("potion", potionType.getRegistryName().toString());

			List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
			if (!effects.isEmpty()) {
				LuaList<Map<String, Object>> effectsInfo = new LuaList<>(effects.size());
				for (PotionEffect effect : effects) {
					Map<String, Object> entry = new HashMap<>();

					entry.put("duration", effect.getDuration() / 20); // ticks!
					entry.put("amplifier", effect.getAmplifier());

					Potion potion = effect.getPotion();
					entry.put("name", potion.getName());
					entry.put("instant", potion.isInstant());
					entry.put("color", potion.getLiquidColor());

					effectsInfo.add(entry);
				}

				data.put("effects", effectsInfo.asMap());
			}

			return data;
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.SWIFTNESS);
		}
	};

	public static final IMetaProvider<ItemStack> ITEM_TOOL_CLASS = new BasicMetaProvider<ItemStack>(
		"Provides the tool classes an item may have."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack stack) {
			Item item = stack.getItem();
			Set<String> toolClasses = item.getToolClasses(stack);
			if (toolClasses.isEmpty()) return Collections.emptyMap();

			HashMap<String, Object> types = new HashMap<>();
			for (String tool : item.getToolClasses(stack)) {
				types.put(tool, item.getHarvestLevel(stack, tool, null, null));
			}
			return Collections.singletonMap("toolClass", types);
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return new ItemStack(Items.DIAMOND_PICKAXE);
		}
	};
}
