package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IAEItemStack.class, modId = AppEng.MOD_ID)
public class ConverterAEItemStack implements IConverter<IAEItemStack, ItemStack> {
	@Nullable
	@Override
	public ItemStack convert(@Nonnull IAEItemStack from) {
		return from.getItemStack();
	}
}
