package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.ArgumentHelper;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.Point2D;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static org.squiddev.plethora.api.method.ArgumentHelper.getTable;

public class ArgumentPointHelper {
	public static Point2D getPoint(@Nonnull Object[] args, int index) throws LuaException {
		return getPoint(getTable(args, index));
	}


	public static Point2D getPoint(Map<?, ?> point) throws LuaException {
		Object xObj, yObj;
		if (point.containsKey("x")) {
			xObj = point.get("x");
			yObj = point.get("y");

			if (!(xObj instanceof Number)) throw badKey(xObj, "x", "number");
			if (!(yObj instanceof Number)) throw badKey(xObj, "y", "number");
		} else {
			xObj = point.get(1.0);
			yObj = point.get(2.0);

			if (!(xObj instanceof Number)) throw badKey(xObj, "1", "number");
			if (!(yObj instanceof Number)) throw badKey(xObj, "2", "number");
		}

		return new Point2D(((Number) xObj).floatValue(), ((Number) yObj).floatValue());
	}

	@Nonnull
	public static LuaException badKey(@Nullable Object object, @Nonnull String key, @Nonnull String expected) {
		return new LuaException("Expected " + expected + " for key " + key + ", got " + ArgumentHelper.getType(object));
	}
}
