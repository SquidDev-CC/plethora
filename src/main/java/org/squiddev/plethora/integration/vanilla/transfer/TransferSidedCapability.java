package org.squiddev.plethora.integration.vanilla.transfer;

import com.google.common.collect.Maps;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Transfer location that provides one side of a capability provider
 */
@ITransferProvider.Inject(value = ICapabilityProvider.class, primary = false)
public class TransferSidedCapability implements ITransferProvider<ICapabilityProvider> {
	private final Map<String, EnumFacing> mappings;

	public TransferSidedCapability() {
		Map<String, EnumFacing> mappings = this.mappings = Maps.newHashMap();
		mappings.put("bottom", EnumFacing.DOWN);
		mappings.put("top", EnumFacing.UP);
	}

	@Nullable
	@Override
	public Object getTransferLocation(final @Nonnull ICapabilityProvider object, @Nonnull String key) {
		EnumFacing facing = EnumFacing.byName(key);
		if (facing == null) {
			facing = mappings.get(key.toLowerCase());
		}

		if (facing != null) {
			final EnumFacing primeFacing = facing;
			return new ICapabilityProvider() {
				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
					return (enumFacing == primeFacing || enumFacing == null) && object.hasCapability(capability, primeFacing);
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
					return (enumFacing == primeFacing || enumFacing == null) ? object.getCapability(capability, primeFacing) : null;
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
