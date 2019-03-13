package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

/**
 * An object which can be scaled. This includes point side, text size and line thickness.
 */
public interface Scalable {
	float getScale();

	void setScale(float scale);

	@PlethoraMethod(doc = "-- Get the scale for this object.", worldThread = false)
	static double getScale(@FromTarget Scalable object) {
		return object.getScale();
	}

	@PlethoraMethod(doc = "-- Set the scale for this object.", worldThread = false)
	static void setScale(@FromTarget Scalable object, float scale) throws LuaException {
		if (scale <= 0) throw new LuaException("Scale must be > 0");
		object.setScale(scale);
	}
}
