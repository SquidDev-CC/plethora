package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * This holds the context for a method.
 *
 * This tracks the current object and all parent/associated objects
 */
public interface IContext<T> extends IPartialContext<T> {
	/**
	 * Make a child context
	 *
	 * @param target          The child's target
	 * @param targetReference A reference to child's target
	 * @return The child context
	 */
	@Nonnull
	<U> IContext<U> makeChild(U target, @Nonnull IReference<U> targetReference);

	/**
	 * Make a child context
	 *
	 * @param target The child's target
	 * @return The child context
	 */
	@Nonnull
	<U extends IReference<U>> IContext<U> makeChild(@Nonnull U target);

	/**
	 * Make a child context, using {@link org.squiddev.plethora.api.reference.Reference#id(Object)}
	 *
	 * @param target The child's target
	 * @return The child context
	 */
	@Nonnull
	<U> IContext<U> makeChildId(@Nonnull U target);

	/**
	 * Get the unbaked context for this context.
	 *
	 * @return The unbaked context.
	 */
	@Nonnull
	IUnbakedContext<T> unbake();

	@Nonnull
	ILuaObject getObject();
}
