package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import javax.annotation.Nonnull;

public interface Positionable3D {
	@Nonnull
	Vec3d getPosition();

	void setPosition(@Nonnull Vec3d position);

	@PlethoraMethod(doc = "function():number, number, number -- Get the position for this object.", worldThread = false)
	static MethodResult getPosition(@FromTarget Positionable3D object) {
		Vec3d pos = object.getPosition();
		return MethodResult.result(pos.x, pos.y, pos.z);
	}

	@PlethoraMethod(doc = "-- Set the position for this object.", worldThread = false)
	static void setPosition(@FromTarget Positionable3D object, double x, double y, double z) {
		object.setPosition(new Vec3d(x, y, z));
	}
}
