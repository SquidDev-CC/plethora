package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import java.util.concurrent.Callable;

public class MethodsVanillaTileEntities {
	@BasicMethod.Inject(value = TileEntityFurnace.class, doc = "function():int -- Number of ticks of fuel left")
	public static MethodResult getRemainingBurnTime(final IUnbakedContext<TileEntityFurnace> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				// furnaceBurnTime
				return MethodResult.result(context.bake().getTarget().getField(0));
			}
		});
	}

	@BasicMethod.Inject(value = TileEntityFurnace.class, doc = "function():int -- Number of ticks of burning the current fuel provides")
	public static MethodResult getBurnTime(final IUnbakedContext<TileEntityFurnace> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				// currentItemBurnTime
				return MethodResult.result(context.bake().getTarget().getField(1));
			}
		});
	}

	@BasicMethod.Inject(value = TileEntityFurnace.class, doc = "function():int -- Number of ticks the current item has cooked for")
	public static MethodResult getCookTime(final IUnbakedContext<TileEntityFurnace> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				// cookTime
				return MethodResult.result(context.bake().getTarget().getField(2));
			}
		});
	}

	@BasicMethod.Inject(value = TileEntityBrewingStand.class, doc = "function():int -- Number of ticks the current potion has brewed for")
	public static MethodResult getBrewTime(final IUnbakedContext<TileEntityBrewingStand> context, Object[] args) {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				// brewTime
				return MethodResult.result(context.bake().getTarget().getField(0));
			}
		});
	}
}
