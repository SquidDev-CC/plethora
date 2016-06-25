package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.converter.Converter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.reference.ItemSlot;

@Converter(ItemSlot.class)
public class ConverterSlot implements IConverter<ItemSlot, ItemStack> {
	@Override
	public ItemStack convert(ItemSlot from) {
		return from.getStack();
	}
}
