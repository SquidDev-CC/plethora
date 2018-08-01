package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.Vec2d;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

/**
 * An object which can be positioned in 2D.
 */
public interface Positionable2D {
	Vec2d getPosition();

	void setPosition(Vec2d position);

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function():number, number -- Get the position for this object.")
	static MethodResult getPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		Vec2d pos = object.getPosition();
		return MethodResult.result(pos.x, pos.y);
	}

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function(x:number, y:number) -- Set the position for this object.")
	static MethodResult setPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		object.setPosition(new Vec2d(getFloat(args, 0), getFloat(args, 1)));
		return MethodResult.empty();
	}
}
