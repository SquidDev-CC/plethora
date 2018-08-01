package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

/**
 * An object which can be coloured.
 */
public interface Colourable {
	int DEFAULT_COLOUR = 0xFFFFFFFF;

	int getColour();

	void setColour(int colour);

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the colour for this object.")
	static MethodResult getColour(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		int colour = context.safeBake().getTarget().getColour();
		return MethodResult.result(colour & 0xffffffffL);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the color for this object.")
	static MethodResult getColor(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		return getColour(context, args);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(colour|r:int, [g:int, b:int], [alpha:int]):number -- Set the colour for this object.")
	static MethodResult setColour(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		Colourable object = context.safeBake().getTarget();
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
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(color|r:int, [g:int, b:int], [alpha:int]):number -- Set the color for this object.")
	static MethodResult setColor(IUnbakedContext<Colourable> object, Object[] args) throws LuaException {
		return setColour(object, args);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the alpha for this object.")
	static MethodResult getAlpha(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().getColour() & 0xFF);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(alpha:int) -- Set the alpha for this object.")
	static MethodResult setAlpha(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		Colourable object = context.safeBake().getTarget();
		object.setColour((object.getColour() & ~0xFF) | (getInt(args, 0) & 0xFF));
		return MethodResult.empty();
	}

}
