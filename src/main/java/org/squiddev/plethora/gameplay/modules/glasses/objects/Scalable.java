package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

/**
 * An object which can be scaled. This includes point side, text size and line thickness.
 */
public interface Scalable {
	float getScale();

	void setScale(float scale);

	@BasicMethod.Inject(value = Scalable.class, doc = "function():number -- Get the scale for this object.")
	static MethodResult getScale(IUnbakedContext<Scalable> context, Object[] args) throws LuaException {
		Scalable object = context.safeBake().getTarget();
		return MethodResult.result(object.getScale());
	}

	@BasicMethod.Inject(value = Scalable.class, doc = "function(scale:number) -- Set the scale for this object.")
	static MethodResult setScale(IUnbakedContext<Scalable> context, Object[] args) throws LuaException {
		Scalable object = context.safeBake().getTarget();

		float thickness = getFloat(args, 0);
		if (thickness <= 0) throw new LuaException("Scale must be > 0");
		object.setScale(thickness);
		return MethodResult.empty();
	}
}
