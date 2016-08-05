package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(ItemSlot.class)
public class ConverterSlot implements IConverter<ItemSlot, ItemStack> {
	@Nullable
	@Override
	public ItemStack convert(@Nonnull ItemSlot from) {
		return from.getStack();
	}
}
