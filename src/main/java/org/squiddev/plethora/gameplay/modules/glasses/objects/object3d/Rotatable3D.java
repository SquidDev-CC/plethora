package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nullable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public interface Rotatable3D {
	@Nullable
	Vec3d getRotation();

	void setRotation(@Nullable Vec3d rotation);


	@BasicMethod.Inject(value = Rotatable3D.class, doc = "function():nil|number, number, number -- Get the rotation for this object, or nil if it faces the player.")
	static MethodResult getRotation(IUnbakedContext<Rotatable3D> context, Object[] args) throws LuaException {
		Rotatable3D object = context.safeBake().getTarget();
		Vec3d pos = object.getRotation();
		return pos == null ? MethodResult.empty() : MethodResult.result(pos.x, pos.y, pos.z);
	}

	@BasicMethod.Inject(value = Rotatable3D.class, doc = "function([x:number, y:number, z:number]) -- Set the rotation for this object, passing nothing if it should face the player.")
	static MethodResult setRotation(IUnbakedContext<Rotatable3D> context, Object[] args) throws LuaException {
		Rotatable3D object = context.safeBake().getTarget();
		object.setRotation(args.length == 0 || (args.length == 1 && args[0] == null)
			? null
			: new Vec3d(getFloat(args, 0), getFloat(args, 1), getFloat(args, 2))
		);
		return MethodResult.empty();
	}
}
