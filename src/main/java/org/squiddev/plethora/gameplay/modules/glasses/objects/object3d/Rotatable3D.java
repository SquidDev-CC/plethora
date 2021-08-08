package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nullable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public interface Rotatable3D {
	@Nullable
	Vec3d getRotation();

	void setRotation(@Nullable Vec3d rotation);

	@PlethoraMethod(doc = "function():nil|number, number, number -- Get the rotation for this object, or nil if it faces the player.", worldThread = false)
	static MethodResult getRotation(@FromTarget Rotatable3D object) {
		Vec3d pos = object.getRotation();
		return pos == null ? MethodResult.empty() : MethodResult.result(pos.x, pos.y, pos.z);
	}

	@PlethoraMethod(doc = "function([x:number, y:number, z:number]) -- Set the rotation for this object, passing nothing if it should face the player.", worldThread = false)
	static void setRotation(@FromTarget Rotatable3D object, @Optional() Vec3d rotation) throws LuaException {
		object.setRotation(rotation);
	}
}
