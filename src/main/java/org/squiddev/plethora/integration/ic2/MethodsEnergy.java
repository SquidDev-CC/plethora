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
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Various methods for interacting with IC2's energy net
 */
public class MethodsEnergy {
	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = IC2.MODID,
		doc = "function():integer -- The amount of EU currently stored"
	)
	public static Object[] getEUStored(IContext<IEnergyStorage> target, Object[] args) {
		return new Object[]{target.getTarget().getStored()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = IC2.MODID,
		doc = "function():integer -- The maximum amount of EU that can be stored"
	)
	public static Object[] getEUCapacity(IContext<IEnergyStorage> target, Object[] args) {
		return new Object[]{target.getTarget().getCapacity()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = IC2.MODID,
		doc = "function():integer -- The maximum EU output per tick"
	)
	public static Object[] getEUOutput(IContext<IEnergyStorage> target, Object[] args) {
		return new Object[]{target.getTarget().getOutputEnergyUnitsPerTick()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergySink.class, modId = IC2.MODID,
		doc = "function():integer -- The maximum amount of EU that can be received"
	)
	public static Object[] getDemandedEnergy(IContext<IEnergySink> target, Object[] args) {
		return new Object[]{target.getTarget().getDemandedEnergy()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergySink.class, modId = IC2.MODID,
		doc = "function():integer -- The tier of this EU sink. 1 = LV, 2 = MV, 3 = HV, 4 = EV etc."
	)
	public static Object[] getSinkTier(IContext<IEnergySink> target, Object[] args) {
		return new Object[]{target.getTarget().getSinkTier()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyConductor.class, modId = IC2.MODID,
		doc = "function():integer -- The EU loss for this conductor"
	)
	public static Object[] getConductionLoss(IContext<IEnergyConductor> target, Object[] args) {
		return new Object[]{target.getTarget().getConductionLoss()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyConductor.class, modId = IC2.MODID,
		doc = "function():integer -- Amount of EU the insulation can handle before shocking players"
	)
	public static Object[] getInsulationEnergyAbsorption(IContext<IEnergyConductor> target, Object[] args) {
		return new Object[]{target.getTarget().getInsulationEnergyAbsorption()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyConductor.class, modId = IC2.MODID,
		doc = "function():integer -- Amount of EU the insulation will handle before it is destroyed"
	)
	public static Object[] getInsulationBreakdownEnergy(IContext<IEnergyConductor> target, Object[] args) {
		return new Object[]{target.getTarget().getInsulationBreakdownEnergy()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyConductor.class, modId = IC2.MODID,
		doc = "function():integer -- Amount of EU the the conductor will handle before it melts"
	)
	public static Object[] getConductorBreakdownEnergy(IContext<IEnergyConductor> target, Object[] args) {
		return new Object[]{target.getTarget().getConductorBreakdownEnergy()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergySource.class, modId = IC2.MODID,
		doc = "function():integer -- EU output provided per tick"
	)
	public static Object[] getOfferedEnergy(IContext<IEnergySource> target, Object[] args) {
		return new Object[]{target.getTarget().getOfferedEnergy()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergySource.class, modId = IC2.MODID,
		doc = "function():integer -- The tier of this EU source. 1 = LV, 2 = MV, 3 = HV, 4 = EV etc."
	)
	public static Object[] getSourceTier(IContext<IEnergySource> target, Object[] args) {
		return new Object[]{target.getTarget().getSourceTier()};
	}

	public static IElectricItemManager getManager(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ISpecialElectricItem) {
			return ((ISpecialElectricItem) item).getManager(stack);
		} else if (item instanceof IElectricItem) {
			return ElectricItem.rawManager;
		} else {
			return ElectricItem.getBackupManager(stack);
		}
	}

	@IMethod.Inject(value = ItemStack.class, modId = IC2.MODID)
	public static final class MethodGetEuStored extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IElectricItem> {
		public MethodGetEuStored() {
			super("getEuStored", true, "function():integer -- The amount of EU currently stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && getManager(context.getTarget()) != null;
		}

		@Nullable
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

	@IMethod.Inject(value = ItemStack.class, modId = IC2.MODID)
	public static final class MethodGetEuCapacity extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IElectricItem> {
		public MethodGetEuCapacity() {
			super("getEuCapacity", true, "function():integer -- The maximum amount of EU that can be stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && getManager(context.getTarget()) != null;
		}

		@Nullable
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
