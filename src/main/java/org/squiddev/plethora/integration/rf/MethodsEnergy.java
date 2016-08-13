package org.squiddev.plethora.integration.rf;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class MethodsEnergy {
	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = "CoFHAPI|energy", worldThread = true, name = "getEnergyStored",
		doc = "function():int -- Get the amount of RF currently stored"
	)
	public static Object[] getEnergyStoredStorage(IContext<IEnergyStorage> context, Object[] args) {
		return new Object[]{context.getTarget().getEnergyStored()};
	}

	@BasicObjectMethod.Inject(
		value = IEnergyStorage.class, modId = "CoFHAPI|energy", worldThread = true, name = "getMaxEnergyStored",
		doc = "function():int -- Get the maximum amount of RF that can be stored"
	)
	public static Object[] getMaxEnergyStoredStorage(IContext<IEnergyStorage> context, Object[] args) {
		return new Object[]{context.getTarget().getMaxEnergyStored()};
	}

	@BasicMethod.Inject(
		value = IEnergyHandler.class, modId = "CoFHAPI|energy", name = "getEnergyStored",
		doc = "function([side:string]):int -- Get the amount of RF currently stored"
	)
	public static MethodResult getEnergyStoredHandler(final IUnbakedContext<IEnergyHandler> context, Object[] args) throws LuaException {
		final EnumFacing facing = parseFacing(ArgumentHelper.optString(args, 0, null));

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				return MethodResult.result(context.bake().getTarget().getEnergyStored(facing));
			}
		});
	}

	@BasicMethod.Inject(
		value = IEnergyHandler.class, modId = "CoFHAPI|energy", name = "getMaxEnergyStored",
		doc = "function([side:string]):int -- Get the maximum amount of RF that can be stored"
	)
	public static MethodResult getMaxEnergyStoredHandler(final IUnbakedContext<IEnergyHandler> context, Object[] args) throws LuaException {
		final EnumFacing facing = parseFacing(ArgumentHelper.optString(args, 0, null));

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				return MethodResult.result(context.bake().getTarget().getMaxEnergyStored(facing));
			}
		});
	}

	private static EnumFacing parseFacing(String direction) throws LuaException {
		if (direction != null) {
			direction = direction.toLowerCase();
			EnumFacing facing = EnumFacing.byName(direction);
			if (facing == null) throw new LuaException("Invalid direction " + direction);

			return facing;
		} else {
			return null;
		}
	}

	@IMethod.Inject(value = ItemStack.class, modId = "CoFHAPI|energy")
	public static final class MethodEnergyStoredItem extends BasicObjectMethod<ItemStack> {
		public MethodEnergyStoredItem() {
			super("getEnergyStored", true, "function([side:string]):int -- Get the amount of RF currently stored");
		}

		@Override
		public boolean canApply(@Nonnull IContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
			ItemStack stack = context.getTarget();
			return new Object[]{((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack)};
		}
	}

	@IMethod.Inject(value = ItemStack.class, modId = "CoFHAPI|energy")
	public static final class MaxMethodEnergyStoredItem extends BasicObjectMethod<ItemStack> {
		public MaxMethodEnergyStoredItem() {
			super("getEnergyMaxStored", true, "function([side:string]):int -- Get the maximum amount of RF that can be stored");
		}

		@Override
		public boolean canApply(@Nonnull IContext<ItemStack> context) {
			return super.canApply(context) && context.getTarget().getItem() instanceof IEnergyContainerItem;
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<ItemStack> context, @Nonnull Object[] args) throws LuaException {
			ItemStack stack = context.getTarget();
			return new Object[]{((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack)};
		}
	}
}
