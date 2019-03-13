package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

public class MethodsEnergy {
	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The amount of RF currently stored")
	public static int getRFStored(@FromTarget IEnergyStorage storage) {
		return storage.getEnergyStored();
	}

	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The maximum amount of RF that can be stored")
	public static int getRFCapacityStored(@FromTarget IEnergyStorage storage) {
		return storage.getMaxEnergyStored();
	}

	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The amount of RF currently stored")
	public static int getRFStored(@FromTarget IEnergyHandler handler, @Optional EnumFacing side) {
		return handler.getEnergyStored(side);
	}

	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The maximum amount of RF that can be stored")
	public static int getRFCapacity(@FromTarget IEnergyHandler handler, @Optional EnumFacing side) {
		return handler.getMaxEnergyStored(side);
	}

	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The amount of RF currently stored")
	public static int getRFStored(@FromTarget ItemStack stack, @FromSubtarget(ContextKeys.TARGET) IEnergyContainerItem item) {
		return item.getEnergyStored(stack);
	}

	@PlethoraMethod(modId = RedstoneFluxProps.MOD_ID, doc = "-- The maximum amount of RF that can be stored")
	public static int getRFCapacity(@FromTarget ItemStack stack, @FromSubtarget(ContextKeys.TARGET) IEnergyContainerItem item) {
		return item.getMaxEnergyStored(stack);
	}
}
