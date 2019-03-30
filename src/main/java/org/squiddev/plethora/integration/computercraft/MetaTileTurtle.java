package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.integration.computercraft.MetaItemTurtle.getUpgrade;

@IMetaProvider.Inject(value = ITurtleTile.class, modId = ComputerCraft.MOD_ID, namespace = "turtle")
public class MetaTileTurtle extends BasicMetaProvider<ITurtleTile> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ITurtleTile object) {
		Map<Object, Object> out = new HashMap<>();

		int colour = object.getColour();
		if (colour != -1) {
			out.put("color", colour);
			out.put("colour", colour); // For those who can spell :p
		}
		out.put("fuel", object.getAccess().getFuelLevel());

		out.put("left", getUpgrade(object.getUpgrade(TurtleSide.Left)));
		out.put("right", getUpgrade(object.getUpgrade(TurtleSide.Right)));

		return out;
	}
}
