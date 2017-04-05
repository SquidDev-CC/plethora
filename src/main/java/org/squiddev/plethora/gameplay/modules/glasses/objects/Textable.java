package org.squiddev.plethora.gameplay.modules.glasses.objects;

import javax.annotation.Nonnull;

/**
 * An object which contains text.
 */
public interface Textable {
	@Nonnull
	String getText();

	void setText(@Nonnull String text);
}
