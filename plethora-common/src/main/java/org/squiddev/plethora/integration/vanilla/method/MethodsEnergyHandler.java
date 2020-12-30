package org.squiddev.plethora.integration.vanilla.method;

import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

public final class MethodsEnergyHandler {
	private MethodsEnergyHandler() {
	}

	@PlethoraMethod(doc = "-- The amount of energy currently stored")
	public static int getEnergyStored(@FromTarget IEnergyStorage storage) {
		return storage.getEnergyStored();
	}

	@PlethoraMethod(doc = "-- The maximum amount of energy that can be stored")
	public static int getEnergyCapacity(@FromTarget IEnergyStorage storage) {
		return storage.getMaxEnergyStored();
	}
}
