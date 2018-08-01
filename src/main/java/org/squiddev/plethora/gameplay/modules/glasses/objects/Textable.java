package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.*;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * An object which contains text.
 */
public interface Textable {
	@Nonnull
	String getText();

	void setText(@Nonnull String text);

	void setShadow(boolean dropShadow);

	boolean hasShadow();

	void setLineHeight(short height);

	short getLineHeight();

	@BasicMethod.Inject(value = Textable.class, doc = "function():string -- Get the text for this object.")
	static MethodResult getText(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();
		return MethodResult.result(object.getText());
	}

	@BasicMethod.Inject(value = Textable.class, doc = "function(text:string) -- Set the text for this object.")
	static MethodResult setText(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();

		String contents = getString(args, 0);
		assertBetween(contents.length(), 0, 512, "string length out of bounds (%s)");
		object.setText(contents);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Textable.class, doc = "function(shadow:boolean):number -- Set the shadow for this object.")
	static MethodResult setShadow(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();

		boolean shadow = getBoolean(args, 0);
		object.setShadow(shadow);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Textable.class, doc = "function():boolean -- Get the shadow for this object.")
	static MethodResult hasShadow(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();
		return MethodResult.result(object.hasShadow());
	}

	@BasicMethod.Inject(value = Textable.class, doc = "function():number -- Get the line height for this object.")
	static MethodResult getLineHeight(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();
		return MethodResult.result(object.getLineHeight());
	}

	@BasicMethod.Inject(value = Textable.class, doc = "function(scale:number) -- Set the line height for this object.")
	static MethodResult setLineHeight(IUnbakedContext<Textable> context, Object[] args) throws LuaException {
		Textable object = context.safeBake().getTarget();

		short lineHeight = (short) getInt(args, 0);
		object.setLineHeight(lineHeight);
		return MethodResult.empty();
	}
}
