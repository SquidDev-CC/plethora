package org.squiddev.plethora.api.method;

import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * Methods to "augment" the current context
 */
public interface IContextBuilder {
	/**
	 * Add an additional object to the current context.
	 *
	 * @param key       The context key to insert under.
	 * @param baked     The object that this reference will return
	 * @param reference A reference to this object
	 * @return The current {@link IContextBuilder}.
	 */
	@Nonnull
	<T> IContextBuilder addContext(@Nonnull String key, @Nonnull T baked, @Nonnull IReference<T> reference);

	/**
	 * Add an additional object to the current context.
	 *
	 * @param key    The context key to insert under.
	 * @param object The object
	 * @return The current {@link IContextFactory}.
	 */
	@Nonnull
	<T extends IReference<T>> IContextBuilder addContext(@Nonnull String key, @Nonnull T object);

	/**
	 * Add an an attachment listener
	 *
	 * @param attachable An attachable object which listens to when the resulting object is attached.
	 * @return The current {@link IContextBuilder}.
	 */
	@Nonnull
	IContextBuilder addAttachable(@Nonnull IAttachable attachable);
}
