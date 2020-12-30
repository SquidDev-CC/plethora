package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.event.TurtleRefuelEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.plethora.gameplay.ConfigGameplay;

import javax.annotation.Nonnull;

final class FERefuelHandler implements TurtleRefuelEvent.Handler {
	public static final FERefuelHandler INSTANCE = new FERefuelHandler();

	private FERefuelHandler() {
	}

	@Override
	public int refuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack, int slot, int limit) {
		IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
		int ratio = ConfigGameplay.Turtle.feFuelRatio;
		if (energy == null || ratio <= 0) return 0;


		// We treat 64 (the default) and the stack size (what the refuel program uses) as magic values for the
		// refuel limit. This'll require further changes to CC:T in the future in order to be friendly.
		if (limit < 0 || limit >= 64 || limit == stack.getCount()) {
			limit = turtle.getFuelLimit() - turtle.getFuelLevel();
		}

		// Pull until there we have drained the cell or pulled everything we need. We do this multiple times
		// to bypass extraction rate limits.
		int energyLimit = limit * ratio;
		int energyExtracted = 0;
		while (energyExtracted < energyLimit) {
			int change = energy.extractEnergy(energyLimit - energyExtracted, false);
			if (change <= 0) break;
			energyExtracted += change;
		}

		return energyExtracted / ratio;
	}
}
