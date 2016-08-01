package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;

@IConverter.Inject(ItemSlot.class)
public class ConverterSlot implements IConverter<ItemSlot, ItemStack> {
	@Nonnull
	@Override
	public ItemStack convert(@Nonnull ItemSlot from) {
		return from.getStack();
	}
}
