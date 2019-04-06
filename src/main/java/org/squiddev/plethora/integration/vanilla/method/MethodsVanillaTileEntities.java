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
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.meta.VanillaMeta;

import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.optString;

public final class MethodsVanillaTileEntities {
	private MethodsVanillaTileEntities() {
	}

	@PlethoraMethod(
		doc = "function():int -- Number of ticks of fuel left",
		worldThread = false
	)
	public static int getRemainingBurnTime(@FromTarget TileEntityFurnace furnace) {
		return furnace.getField(0); // furnaceBurnTime
	}

	@PlethoraMethod(
		doc = "function():int -- Number of ticks of burning the current fuel provides",
		worldThread = false
	)
	public static int getBurnTime(@FromTarget TileEntityFurnace furnace) {
		return furnace.getField(1); // currentItemBurnTime
	}

	@PlethoraMethod(
		doc = "function():int -- Number of ticks the current item has cooked for",
		worldThread = false
	)
	public static int getCookTime(@FromTarget TileEntityFurnace furnace) {
		return furnace.getField(2); // cookTime
	}

	@PlethoraMethod(
		doc = "function():int -- Number of ticks the current potion has brewed for",
		worldThread = false
	)
	public static int getBrewTime(@FromTarget TileEntityBrewingStand brewing) {
		return brewing.getField(0); // brewTime
	}

	@PlethoraMethod(doc = "function():table -- Each line of text on this sign")
	public static Map<Integer, String> getSignText(@FromTarget TileEntitySign sign) {
		return VanillaMeta.getSignLines(sign);
	}

	@PlethoraMethod(doc = "function(lines...:string) -- Set the lines of text on this sign")
	public static void setSignText(@FromTarget TileEntitySign sign, Object[] args) throws LuaException {
		final ITextComponent[] lines = new ITextComponent[4];
		for (int i = 0; i < lines.length; i++) {
			String arg = optString(args, i, "");

			// This may seem rather large but it is possible to get quite large when using very narrow letters.
			if (arg.length() > 64) {
				throw new LuaException("Expected length <= 64 for argument (" + (i + 1) + "), got " + arg.length());
			}

			lines[i] = new TextComponentString(arg);
		}

		System.arraycopy(lines, 0, sign.signText, 0, lines.length);
		sign.markDirty();

		World world = sign.getWorld();
		BlockPos pos = sign.getPos();
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}
}
