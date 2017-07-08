package org.squiddev.plethora.integration.cctweaks;

import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Get all nodes from a modem peripheral.
 */
@ITransferProvider.Inject(value = BasicModemPeripheral.class, modId = CCTweaks.ID)
public class TransferNetworkNode implements ITransferProvider<BasicModemPeripheral> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull BasicModemPeripheral object, @Nonnull String key) {
		INetworkController access = object.modem.getAttachedNetwork();
		if (access == null) return null;

		return access.getPeripheralsOnNetwork().get(key);
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull BasicModemPeripheral object) {
		INetworkController access = object.modem.getAttachedNetwork();
		return access == null ? Collections.emptySet() : access.getPeripheralsOnNetwork().keySet();
	}
}
