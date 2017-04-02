package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.GlassesInstance;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.Rectangle;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

public class MethodsCanvas {
	@SubtargetedModuleMethod.Inject(
		target = GlassesInstance.class, module = PlethoraModules.GLASSES_S,
		doc = "function():table -- Get the canvas for these glasses."
	)
	public static MethodResult canvas(IUnbakedContext<IModuleContainer> context, Object[] args) throws LuaException {
		IContext<IModuleContainer> baked = context.safeBake();
		GlassesInstance server = baked.getContext(GlassesInstance.class);
		return MethodResult.result(baked.makeChild(server.getCanvas()).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(int:id) -- Remove this given object from the canvas.")
	public static MethodResult removeObject(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		CanvasServer canvas = context.safeBake().getTarget();

		BaseObject object = canvas.getObject(getInt(args, 0));
		if (object == null) throw new LuaException("No such object");
		canvas.remove(object);

		return MethodResult.result();
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(x:number, y:number, width:number, height:number[, color:number]):table -- Create a new rectangle")
	public static MethodResult addRectangle(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		float x = getFloat(args, 0);
		float y = getFloat(args, 1);
		float width = getFloat(args, 2);
		float height = getFloat(args, 3);
		int colour = optInt(args, 4, 0xFFFFFFFF);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Rectangle rectangle = new Rectangle(canvas.newObjectId());
		rectangle.setPosition(x, y);
		rectangle.setSize(width, height);
		rectangle.setColour(colour);

		canvas.add(rectangle);

		return MethodResult.result(baked.makeChild(rectangle.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function() -- Clear this canvas.")
	public static MethodResult clear(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake().getTarget().clear();
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function():number, number -- Get the size of this canvas.")
	public static MethodResult getSize(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake();
		return MethodResult.result(CanvasHandler.WIDTH, CanvasHandler.HEIGHT);
	}
}
