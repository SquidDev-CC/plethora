package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.squiddev.plethora.api.converter.Converter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;

@Converter(IInventory.class)
public class ConverterItemHandler implements IConverter<IInventory, IItemHandler> {
	@Nonnull
	@Override
	public IItemHandler convert(@Nonnull IInventory from) {
		return new InvWrapper(from);
	}
}
