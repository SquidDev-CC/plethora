package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyStorage;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class MethodsEnergy {
	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = "CoFHAPI|energy", worldThread = true, name = "getRFStored",
		doc = "function():int -- The amount of RF currently stored"
	)
	public static Object[] getRFStoredStorage(IContext<IEnergyStorage> context, Object[] args) {
		return new Object[]{context.getTarget().getEnergyStored()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = "CoFHAPI|energy", worldThread = true, name = "getRFCapacity",
		doc = "function():int -- The maximum amount of RF that can be stored"
	)
	public static Object[] getRFCapacityStoredStorage(IContext<IEnergyStorage> context, Object[] args) {
		return new Object[]{context.getTarget().getMaxEnergyStored()};
	}

	@BasicMethod.Inject(
		value = IEnergyHandler.class, modId = "CoFHAPI|energy", name = "getRFStored",
		doc = "function([side:string]):int -- The amount of RF currently stored"
	)
	public static MethodResult getRFStoredHandler(final IUnbakedContext<IEnergyHandler> context, Object[] args) throws LuaException {
		final EnumFacing facing = ArgumentHelper.optEnum(args, 0, EnumFacing.class, null);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				return MethodResult.result(context.bake().getTarget().getEnergyStored(facing));
			}
		});
	}

	@BasicMethod.Inject(
		value = IEnergyHandler.class, modId = "CoFHAPI|energy", name = "getRFCapacity",
		doc = "function([side:string]):int -- The maximum amount of RF that can be stored"
	)
	public static MethodResult getRFCapacityHandler(final IUnbakedContext<IEnergyHandler> context, Object[] args) throws LuaException {
		final EnumFacing facing = ArgumentHelper.optEnum(args, 0, EnumFacing.class, null);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				return MethodResult.result(context.bake().getTarget().getMaxEnergyStored(facing));
			}
		});
	}

	@IMethod.Inject(value = ItemStack.class, modId = "CoFHAPI|energy")
	public static final class MethodEnergyStoredItem extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IEnergyContainerItem> {
		public MethodEnergyStoredItem() {
			super("getRFStored", true, "function([side:string]):int -- The amount of RF currently stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
			ItemStack stack = context.getTarget();
			return new Object[]{((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack)};
		}

		@Nonnull
		@Override
		public Class<IEnergyContainerItem> getSubTarget() {
			return IEnergyContainerItem.class;
		}
	}

	@IMethod.Inject(value = ItemStack.class, modId = "CoFHAPI|energy")
	public static final class MaxMethodEnergyStoredItem extends BasicObjectMethod<ItemStack> implements ISubTargetedMethod<ItemStack, IEnergyContainerItem> {
		public MaxMethodEnergyStoredItem() {
			super("getRFCapacity", true, "function([side:string]):int -- The maximum amount of RF that can be stored");
		}

		@Override
		public boolean canApply(@Nonnull IPartialContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
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
