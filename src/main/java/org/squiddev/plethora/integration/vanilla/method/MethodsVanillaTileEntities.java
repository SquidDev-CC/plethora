package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.integration.vanilla.meta.MetaTileSign;

import java.util.concurrent.Callable;

public class MethodsVanillaTileEntities {
	@BasicObjectMethod.Inject(
		value = TileEntityFurnace.class, worldThread = false,
		doc = "function():int -- Number of ticks of fuel left"
	)
	public static Object[] getRemainingBurnTime(final IContext<TileEntityFurnace> context, Object[] args) {
		// furnaceBurnTime
		return new Object[]{context.getTarget().getField(0)};
	}

	@BasicObjectMethod.Inject(
		value = TileEntityFurnace.class, worldThread = false,
		doc = "function():int -- Number of ticks of burning the current fuel provides"
	)
	public static Object[] getBurnTime(final IContext<TileEntityFurnace> context, Object[] args) {
		// currentItemBurnTime
		return new Object[]{context.getTarget().getField(1)};
	}

	@BasicObjectMethod.Inject(
		value = TileEntityFurnace.class, worldThread = false,
		doc = "function():int -- Number of ticks the current item has cooked for"
	)
	public static Object[] getCookTime(final IContext<TileEntityFurnace> context, Object[] args) {
		// cookTime
		return new Object[]{context.getTarget().getField(2)};
	}

	@BasicObjectMethod.Inject(
		value = TileEntityBrewingStand.class, worldThread = false,
		doc = "function():int -- Number of ticks the current potion has brewed for"
	)
	public static Object[] getBrewTime(final IContext<TileEntityBrewingStand> context, Object[] args) {
		// brewTime
		return new Object[]{context.getTarget().getField(0)};
	}

	@BasicObjectMethod.Inject(
		value = TileEntitySign.class, worldThread = true,
		doc = "function():table -- Each line of text on this sign"
	)
	public static Object[] getSignText(final IContext<TileEntitySign> context, Object[] args) {
		return new Object[]{MetaTileSign.getSignLines(context.getTarget())};
	}

	@BasicMethod.Inject(
		value = TileEntitySign.class,
		doc = "function(lines...:string) -- Set the lines of text on this sign"
	)
	public static MethodResult setSignText(final IUnbakedContext<TileEntitySign> context, Object[] args) throws LuaException {
		final ITextComponent[] lines = new ITextComponent[4];
		for (int i = 0; i < lines.length; i++) {
			String arg = ArgumentHelper.optString(args, i, "");

			// This may seem rather large but it is possible to get quite large when using very narrow letters.
			if (arg.length() > 64) {
				throw new LuaException("Expected length <= 64 for argument (" + (i + 1) + "), got " + arg.length());
			}

			lines[i] = new TextComponentString(arg);
		}

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				TileEntitySign sign = context.bake().getTarget();
				System.arraycopy(lines, 0, sign.signText, 0, lines.length);
				sign.markDirty();

				World world = sign.getWorld();
				BlockPos pos = sign.getPos();
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 3);

				return MethodResult.empty();
			}
		});
	}
}
