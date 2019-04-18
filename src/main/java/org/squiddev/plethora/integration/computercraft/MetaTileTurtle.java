package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.blocks.TileTurtleAdvanced;
import net.minecraft.util.math.BlockPos;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.integration.computercraft.MetaItemTurtle.getUpgrade;

@Injects(ComputerCraft.MOD_ID)
public final class MetaTileTurtle extends BasicMetaProvider<ITurtleTile> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ITurtleTile object) {
		Map<String, Object> out = new HashMap<>();

		int colour = object.getColour();
		if (colour != -1) {
			out.put("color", colour);
			out.put("colour", colour); // For those who can spell :p
		}
		out.put("fuel", object.getAccess().getFuelLevel());

		out.put("left", getUpgrade(object.getUpgrade(TurtleSide.Left)));
		out.put("right", getUpgrade(object.getUpgrade(TurtleSide.Right)));

		return Collections.singletonMap("turtle", out);
	}

	@Nonnull
	@Override
	public ITurtleTile getExample() {
		TileTurtleAdvanced turtle = new TileTurtleAdvanced();
		turtle.setWorld(WorldDummy.INSTANCE);
		turtle.setPos(BlockPos.ORIGIN);
		turtle.getAccess().setFuelLevel(100);
		turtle.getAccess().setColour(23);
		return turtle;
	}
}
