package org.squiddev.plethora.integration.vanilla.transfer;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.utils.CapabilityWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Transfer location that provides one side of a capability provider
 *
 * We block primary accesses as they end up being rather noisy
 */
@Injects
public final class TransferSidedCapability implements ITransferProvider<ICapabilityProvider> {
	private final Map<String, EnumFacing> mappings;

	public TransferSidedCapability() {
		Map<String, EnumFacing> mappings = this.mappings = new HashMap<>();
		mappings.put("bottom_side", EnumFacing.DOWN);
		mappings.put("top_side", EnumFacing.UP);
		for (EnumFacing facing : EnumFacing.VALUES) {
			mappings.put(facing.getName() + "_side", facing);
		}
	}

	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull ICapabilityProvider object, @Nonnull String key) {
		final EnumFacing facing = mappings.get(key.toLowerCase());
		return facing == null ? null : new CapabilityWrapper(object, facing);
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull ICapabilityProvider object) {
		HashSet<String> items = new HashSet<>(6);
		for (EnumFacing item : EnumFacing.VALUES) {
			items.add(item.getName() + "_side");
		}

		return items;
	}

	@Override
	public boolean primary() {
		return false;
	}
}
