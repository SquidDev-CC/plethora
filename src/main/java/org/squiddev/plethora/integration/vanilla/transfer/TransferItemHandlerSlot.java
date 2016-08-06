package org.squiddev.plethora.integration.vanilla.transfer;

import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Transfer location that allows accessing child slots
 */
@ITransferProvider.Inject(value = IItemHandler.class, primary = false)
public class TransferItemHandlerSlot implements ITransferProvider<IItemHandler> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull IItemHandler object, @Nonnull String key) {
		try {
			int value = Integer.parseInt(key) - 1;
			if (value >= 0 && value < object.getSlots()) {
				return object.getStackInSlot(value);
			}
		} catch (NumberFormatException ignored) {
		}

		return null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull IItemHandler object) {
		return Collections.emptySet();
	}
}
