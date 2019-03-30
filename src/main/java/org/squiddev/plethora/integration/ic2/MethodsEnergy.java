package org.squiddev.plethora.integration.ic2;

import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.IElectricItemManager;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

/**
 * Various methods for interacting with IC2's energy net
 */
public final class MethodsEnergy {
	private MethodsEnergy() {
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The amount of EU currently stored")
	public static int getEUStored(@FromTarget IEnergyStorage storage) {
		return storage.getStored();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The maximum amount of EU that can be stored")
	public static int getEUCapacity(@FromTarget IEnergyStorage storage) {
		return storage.getCapacity();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The maximum EU output per tick")
	public static double getEUOutput(@FromTarget IEnergyStorage storage) {
		return storage.getOutputEnergyUnitsPerTick();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The maximum amount of EU that can be received")
	public static double getDemandedEnergy(@FromTarget IEnergySink sink) {
		return sink.getDemandedEnergy();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The tier of this EU sink. 1 = LV, 2 = MV, 3 = HV, 4 = EV etc.")
	public static int getSinkTier(@FromTarget IEnergySink sink) {
		return sink.getSinkTier();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The EU loss for this conductor")
	public static double getConductionLoss(@FromTarget IEnergyConductor conductor) {
		return conductor.getConductionLoss();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Amount of EU the insulation can handle before shocking players")
	public static double getInsulationEnergyAbsorption(@FromTarget IEnergyConductor conductor) {
		return conductor.getInsulationEnergyAbsorption();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Amount of EU the insulation will handle before it is destroyed")
	public static double getInsulationBreakdownEnergy(@FromTarget IEnergyConductor conductor) {
		return conductor.getInsulationBreakdownEnergy();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Amount of EU the the conductor will handle before it melts")
	public static double getConductorBreakdownEnergy(@FromTarget IEnergyConductor condutor) {
		return condutor.getConductorBreakdownEnergy();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- EU output provided per tick")
	public static double getOfferedEnergy(@FromTarget IEnergySource source) {
		return source.getOfferedEnergy();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The tier of this EU source. 1 = LV, 2 = MV, 3 = HV, 4 = EV etc.")
	public static int getSourceTier(@FromTarget IEnergySource tier) {
		return tier.getSourceTier();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The amount of EU currently stored.")
	public static double getEuStored(@FromTarget ItemStack stack, @FromSubtarget(ContextKeys.TARGET) IElectricItemManager manager) {
		return manager.getCharge(stack);
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- The maximum amount of EU that can be stored.")
	public static double getEuCapacity(@FromTarget ItemStack stack, @FromSubtarget(ContextKeys.TARGET) IElectricItemManager manager) {
		return manager.getMaxCharge(stack);
	}
}
