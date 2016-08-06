package org.squiddev.plethora.integration.vanilla.transfer;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer location that provides one side of a capability provider
 */
@ITransferProvider.Inject(value = ICapabilityProvider.class, primary = false)
public class TransferSidedCapability implements ITransferProvider<ICapabilityProvider> {
	@Nullable
	@Override
	public Object getTransferLocation(final @Nonnull ICapabilityProvider object, @Nonnull String key) {
		final EnumFacing facing = EnumFacing.byName(key);
		if (facing != null) {
			return new ICapabilityProvider() {
				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
					return enumFacing == facing && object.hasCapability(capability, enumFacing);
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
					return enumFacing == facing ? object.getCapability(capability, enumFacing) : null;
				}
			};
		}

		return null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull ICapabilityProvider object) {
		HashSet<String> items = new HashSet<String>(6);
		for (EnumFacing item : EnumFacing.VALUES) {
			items.add(item.getName2());
		}

		return items;
	}
}
