package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.ISubTargetedMethod;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MethodsEnergy {
	@PlethoraMethod(
		modId = RedstoneFluxProps.MOD_ID,
		doc = "function():int -- The amount of RF currently stored"
	)
	public static int getRFStored(@FromTarget IEnergyStorage storage) {
		return storage.getEnergyStored();
	}

	@PlethoraMethod(
		modId = RedstoneFluxProps.MOD_ID,
		doc = "function():int -- The maximum amount of RF that can be stored"
	)
	public static int getRFCapacityStored(@FromTarget IEnergyStorage storage) {
		return storage.getMaxEnergyStored();
	}

	@PlethoraMethod(
		modId = RedstoneFluxProps.MOD_ID,
		doc = "function([side:string]):int -- The amount of RF currently stored"
	)
	public static int getRFStored(@FromTarget IEnergyHandler handler, @Nullable EnumFacing side) {
		return handler.getEnergyStored(side);
	}

	@PlethoraMethod(
		modId = RedstoneFluxProps.MOD_ID,
		doc = "function([side:string]):int -- The maximum amount of RF that can be stored"
	)
	public static int getRFCapacity(@FromTarget IEnergyHandler handler, @Nullable EnumFacing side) {
		return handler.getMaxEnergyStored(side);
	}

	@Injects(RedstoneFluxProps.MOD_ID)
	public static final class MethodEnergyStoredItem extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IEnergyContainerItem> {
		public MethodEnergyStoredItem() {
			super("getRFStored", true, "function([side:string]):int -- The amount of RF currently stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nonnull
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) {
			ItemStack stack = context.getTarget();
			return new Object[]{((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack)};
		}

		@Nonnull
		@Override
		public Class<IEnergyContainerItem> getSubTarget() {
			return IEnergyContainerItem.class;
		}
	}

	@Injects(RedstoneFluxProps.MOD_ID)
	public static final class MaxMethodEnergyStoredItem extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IEnergyContainerItem> {
		public MaxMethodEnergyStoredItem() {
			super("getRFCapacity", true, "function([side:string]):int -- The maximum amount of RF that can be stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nonnull
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) {
			ItemStack stack = context.getTarget();
			return new Object[]{((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack)};
		}

		@Nonnull
		@Override
		public Class<IEnergyContainerItem> getSubTarget() {
			return IEnergyContainerItem.class;
		}
	}
}
