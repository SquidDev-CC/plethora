package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects
public final class ConverterSlot extends ConstantConverter<ItemSlot, ItemStack> {
	@Nullable
	@Override
	public ItemStack convert(@Nonnull ItemSlot from) {
		return from.getStack();
	}
}
