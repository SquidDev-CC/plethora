package org.squiddev.plethora.integration.cctweaks;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Unwraps peripherals and returns their target
 */
@IConverter.Inject(value = IPeripheral.class, modId = "CCTweaks")
public class ConverterTargetedPeripheral implements IConverter<IPeripheral, Object> {
	@Nullable
	@Override
	public Object convert(@Nonnull IPeripheral from) {
		return CCTweaksAPI.instance().peripheralHelpers().getTarget(from);
	}
}
