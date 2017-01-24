package org.squiddev.plethora.api.method;

import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * Methods to "augment" the current context
 */
public interface IContextBuilder {
	/**
	 * Add an additional object to the current context.
	 *
	 * @param baked     The object that this reference will return
	 * @param reference A reference to this object
	 */
	<T> void addContext(@Nonnull T baked, @Nonnull IReference<T> reference);

	/**
	 * Add an additional object to the current context.
	 *
	 * @param object The object
	 */
	<T extends IReference<T>> void addContext(@Nonnull T object);
}
