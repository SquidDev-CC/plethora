package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Transfer providers for CC: Tweaked's (and hopefully CC's) wired networks.
 */
@Injects(ComputerCraft.MOD_ID)
public final class TransferComputerAccess implements ITransferProvider<IComputerAccess> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull IComputerAccess object, @Nonnull String key) {
		IPeripheral peripheral = object.getAvailablePeripheral(key);
		return peripheral == null ? null : peripheral.getTarget();
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull IComputerAccess object) {
		return object.getAvailablePeripherals().keySet();
	}
}
