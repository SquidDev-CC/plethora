package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

import javax.annotation.Nonnull;

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

	@PlethoraMethod(doc = "-- Get the text for this object.", worldThread = false)
	static String getText(@FromTarget Textable object) {
		return object.getText();
	}

	@PlethoraMethod(doc = "-- Set the text for this object.", worldThread = false)
	static void setText(@FromTarget Textable object, String contents) throws LuaException {
		assertBetween(contents.length(), 0, 512, "string length out of bounds (%s)");
		object.setText(contents);
	}

	@PlethoraMethod(doc = "-- Set the shadow for this object.", worldThread = false)
	static void setShadow(@FromTarget Textable object, boolean shadow) {
		object.setShadow(shadow);
	}

	@PlethoraMethod(doc = "-- Get the shadow for this object.", worldThread = false)
	static boolean hasShadow(@FromTarget Textable object) {
		return object.hasShadow();
	}

	@PlethoraMethod(doc = "-- Get the line height for this object.", worldThread = false)
	static int getLineHeight(@FromTarget Textable object) {
		return object.getLineHeight();
	}

	@PlethoraMethod(doc = "-- Set the line height for this object.", worldThread = false)
	static void setLineHeight(@FromTarget Textable object, short lineHeight) {
		object.setLineHeight(lineHeight);
	}
}
