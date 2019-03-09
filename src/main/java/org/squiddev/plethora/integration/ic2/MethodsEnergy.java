package org.squiddev.plethora.integration.ic2;

import dan200.computercraft.api.lua.LuaException;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.ISubTargetedMethod;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import javax.annotation.Nonnull;

/**
 * Various methods for interacting with IC2's energy net
 */
public class MethodsEnergy {
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

	@PlethoraMethod(modId = IC2.MODID,doc = "-- The tier of this EU source. 1 = LV, 2 = MV, 3 = HV, 4 = EV etc.")
	public static int getSourceTier(@FromTarget IEnergySource tier) {
		return tier.getSourceTier();
	}

	static IElectricItemManager getManager(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ISpecialElectricItem) {
			return ((ISpecialElectricItem) item).getManager(stack);
		} else if (item instanceof IElectricItem) {
			return ElectricItem.rawManager;
		} else {
			return ElectricItem.getBackupManager(stack);
		}
	}

	@Injects(IC2.MODID)
	public static final class MethodGetEuStored extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IElectricItem> {
		public MethodGetEuStored() {
			super("getEuStored", true, "function():integer -- The amount of EU currently stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && getManager(context.getTarget()) != null;
		}

		@Nonnull
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
			ItemStack stack = context.getTarget();
			IElectricItemManager manager = getManager(stack);
			if (manager == null) throw new LuaException("Not an electric item");
			return new Object[]{manager.getCharge(stack)};
		}

		@Nonnull
		@Override
		public Class<IElectricItem> getSubTarget() {
			return IElectricItem.class;
		}
	}

	@Injects(IC2.MODID)
	public static final class MethodGetEuCapacity extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IElectricItem> {
		public MethodGetEuCapacity() {
			super("getEuCapacity", true, "function():integer -- The maximum amount of EU that can be stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && getManager(context.getTarget()) != null;
		}

		@Nonnull
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
			ItemStack stack = context.getTarget();
			IElectricItemManager manager = getManager(stack);
			if (manager == null) throw new LuaException("Not an electric item");
			return new Object[]{manager.getMaxCharge(stack)};
		}

		@Nonnull
		@Override
		public Class<IElectricItem> getSubTarget() {
			return IElectricItem.class;
		}
	}
}
