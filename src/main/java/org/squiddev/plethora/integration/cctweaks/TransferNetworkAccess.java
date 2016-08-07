package org.squiddev.plethora.integration.cctweaks;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Get all directions a method can be transferred
 */
@ITransferProvider.Inject(value = INetworkAccess.class, modId = "CCTweaks")
public class TransferNetworkAccess implements ITransferProvider<INetworkAccess> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull INetworkAccess object, @Nonnull String key) {
		IPeripheral peripheral = object.getPeripheralsOnNetwork().get(key);
		if (peripheral == null) return null;

		IPeripheralHelpers helpers = CCTweaksAPI.instance().peripheralHelpers();

		Object target = helpers.getTarget(peripheral);
		if (target != null) return target;
		return helpers.getBasePeripheral(peripheral);
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull INetworkAccess object) {
		return object.getPeripheralsOnNetwork().keySet();
	}
}
