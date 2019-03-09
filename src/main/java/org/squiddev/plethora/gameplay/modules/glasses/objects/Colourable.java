package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

/**
 * An object which can be coloured.
 */
public interface Colourable {
	int DEFAULT_COLOUR = 0xFFFFFFFF;

	int getColour();

	void setColour(int colour);

	@PlethoraMethod(name = {"getColour", "getColor"}, doc = "-- Get the colour for this object.", worldThread = false)
	static long getColour(@FromTarget Colourable colourable) {
		int colour = colourable.getColour();
		return colour & 0xffffffffL;
	}

	@PlethoraMethod(worldThread = false,
		name = {"setColor", "setColour"},
		doc = "function(colour|r:int, [g:int, b:int], [alpha:int]):number -- Set the colour for this object."
	)
	static void setColour(@FromTarget Colourable object, Object[] args) throws LuaException {
		switch (args.length) {
			case 1:
				object.setColour(getInt(args, 0));
				break;
			case 3: {
				int r = getInt(args, 0) & 0xFF;
				int g = getInt(args, 1) & 0xFF;
				int b = getInt(args, 2) & 0xFF;

				object.setColour((r << 24) | (g << 16) | (b << 8) | object.getColour() & 0xFF);
				break;
			}

			default:
			case 4: {
				int r = getInt(args, 0) & 0xFF;
				int g = getInt(args, 1) & 0xFF;
				int b = getInt(args, 2) & 0xFF;
				int a = getInt(args, 3) & 0xFF;

				object.setColour((r << 24) | (g << 16) | (b << 8) | a);
				break;
			}
		}
	}

	@PlethoraMethod(doc = "-- Get the alpha for this object.", worldThread = false)
	static int getAlpha(@FromTarget Colourable colourable) {
		return colourable.getColour() & 0xFF;
	}

	@PlethoraMethod(doc = "-- Set the alpha for this object.", worldThread = false)
	static void setAlpha(@FromTarget Colourable object, int alpha) {
		object.setColour((object.getColour() & ~0xFF) | (alpha & 0xFF));
	}
}
