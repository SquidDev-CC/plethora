package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta provider for amour properties. Material is handled in {@link MetaItemMaterial}.
 */
@Injects
public final class MetaItemArmor extends ItemStackMetaProvider<ItemArmor> {
	public MetaItemArmor() {
		super(ItemArmor.class, "Provides type and colour of amour.");
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull ItemArmor armor) {
		HashMap<String, Object> data = new HashMap<>(3);
		data.put("armorType", armor.armorType.getName());

		int color = armor.getColor(stack);
		if (color >= 0) {
			data.put("color", color);
			data.put("colour", color);
		}

		return data;
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
		Items.LEATHER_CHESTPLATE.setColor(stack, 0xFF0000);
		return stack;
	}
}
