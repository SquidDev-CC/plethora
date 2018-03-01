package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

public interface IContextFactory<T> {
	/**
	 * Add an additional object to the current context.
	 *
	 * @param key       The context key to insert under.
	 * @param baked     The object that this reference will return
	 * @param reference A reference to this object
	 * @return The current {@link IContextFactory}.
	 */
	@Nonnull
	<U> IContextFactory<T> addContext(@Nonnull String key, @Nonnull U baked, @Nonnull IReference<U> reference);

	/**
	 * Add an additional object to the current context.
	 *
	 * @param key    The context key to insert under.
	 * @param object The object
	 * @return The current {@link IContextFactory}.
	 */
	@Nonnull
	<U extends IReference<U>> IContextFactory<T> addContext(@Nonnull String key, @Nonnull U object);

	@Nonnull
	IContextFactory<T> withCostHandler(@Nonnull ICostHandler handler);

	@Nonnull
	IContextFactory<T> withExecutor(@Nonnull IResultExecutor executor);

	/**
	 * Create a new context with different modules but the same context
	 *
	 * @param modules   A container for available modules.
	 * @param reference A reference which will all modules for this context. This must return {@code modules} when evaluated.
	 * @return The current {@link IContextFactory}.
	 */
	@Nonnull
	IContextFactory<T> withModules(@Nonnull IModuleContainer modules, @Nonnull IReference<IModuleContainer> reference);

	/**
	 * Construct a baked context from this factory
	 *
	 * @return The baked context
	 */
	@Nonnull
	IContext<T> getBaked();

	/**
	 * Construct an unbaked context from this factory
	 *
	 * @return The unbaked context
	 */
	@Nonnull
	IUnbakedContext<T> getUnbaked();

	/**
	 * Get a lua object from this context
	 *
	 * @return The built Lua object
	 */
	@Nonnull
	ILuaObject getObject();
}
