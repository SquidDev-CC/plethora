package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Positionable2D;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;
import static org.squiddev.plethora.api.method.ArgumentHelper.getInt;

public class MethodsBasic {
	@BasicMethod.Inject(value = BaseObject.class, doc = "function():int -- Get the id for this object.")
	public static MethodResult getID(IUnbakedContext<BaseObject> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().id);
	}

	@BasicMethod.Inject(value = BaseObject.class, doc = "function() -- Remove this object from the canvas.")
	public static MethodResult remove(IUnbakedContext<BaseObject> context, Object[] args) throws LuaException {
		IContext<BaseObject> baked = context.safeBake();
		baked.getContext(CanvasServer.class).remove(baked.getTarget());
		return MethodResult.result();
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the colour for this object.")
	public static MethodResult getColour(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().getColour());
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the color for this object.")
	public static MethodResult getColor(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		return getColour(context, args);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(colour|r:int, [g:int, b:int], [alpha:int]):number -- Set the colour for this object.")
	public static MethodResult setColour(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
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

			case 4: {
				int r = getInt(args, 0) & 0xFF;
				int g = getInt(args, 1) & 0xFF;
				int b = getInt(args, 3) & 0xFF;
				int a = getInt(args, 4) & 0xFF;

				object.setColour((r << 24) | (g << 16) | (b << 8) | a);
				break;
			}
		}
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(color|r:int, [g:int, b:int], [alpha:int]):number -- Set the color for this object.")
	public static MethodResult setColor(IUnbakedContext<Colourable> object, Object[] args) throws LuaException {
		return setColour(object, args);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function():int -- Get the alpha for this object.")
	public static MethodResult getAlpha(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().getColour() & 0xFF);
	}

	@BasicMethod.Inject(value = Colourable.class, doc = "function(alpha:int) -- Set the alpha for this object.")
	public static MethodResult setAlpha(IUnbakedContext<Colourable> context, Object[] args) throws LuaException {
		Colourable object = context.safeBake().getTarget();
		object.setColour((object.getColour() & ~0xFF) | (getInt(args, 0) & 0xFF));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function():number, number -- Get the position for this object.")
	public static MethodResult getPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		return MethodResult.result(object.getX(), object.getY());
	}

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function(x:number, y:number) -- Set the position for this object.")
	public static MethodResult setPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		object.setPosition(getFloat(args, 0), getFloat(args, 1));
		return MethodResult.empty();
	}
}
