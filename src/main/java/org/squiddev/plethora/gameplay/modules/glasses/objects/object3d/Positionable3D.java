package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public interface Positionable3D {
	@Nonnull
	Vec3d getPosition();

	void setPosition(@Nonnull Vec3d position);

	@BasicMethod.Inject(value = Positionable3D.class, doc = "function():number, number, number -- Get the position for this object.")
	static MethodResult getPosition(IUnbakedContext<Positionable3D> context, Object[] args) throws LuaException {
		Positionable3D object = context.safeBake().getTarget();
		Vec3d pos = object.getPosition();
		return MethodResult.result(pos.x, pos.y, pos.z);
	}

	@BasicMethod.Inject(value = Positionable3D.class, doc = "function(x:number, y:number, z:number) -- Set the position for this object.")
	static MethodResult setPosition(IUnbakedContext<Positionable3D> context, Object[] args) throws LuaException {
		Positionable3D object = context.safeBake().getTarget();
		object.setPosition(new Vec3d(getFloat(args, 0), getFloat(args, 1), getFloat(args, 2)));
		return MethodResult.empty();
	}
}
