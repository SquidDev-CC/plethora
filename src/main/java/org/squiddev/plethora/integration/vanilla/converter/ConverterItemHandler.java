package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(IInventory.class)
public class ConverterItemHandler implements IConverter<IInventory, IItemHandler> {
	@Nullable
	@Override
	public IItemHandler convert(@Nonnull IInventory from) {
		return new InvWrapper(from);
	}
}
