package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Converts inventories to item handlers if not covered by {@link ConverterItemHandler}.
 *
 * This is mostly for legacy {@link net.minecraft.tileentity.TileEntity}s which don't provide capabilities.
 */
@IConverter.Inject(IInventory.class)
public class ConverterInventory extends ConstantConverter<IInventory, IItemHandler> {
	@Nullable
	@Override
	public IItemHandler convert(@Nonnull IInventory from) {
		if (from instanceof IItemHandler) {
			return (IItemHandler) from;
		}

		// We could do this in ConverterItemHandler, but this is a constant so prefer it here.
		if (from instanceof ICapabilityProvider && ((ICapabilityProvider) from).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
			IItemHandler handler = ((ICapabilityProvider) from).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (handler != null) return handler;
		}

		return new InvWrapper(from);
	}
}
