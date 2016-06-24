package org.squiddev.plethora.api.method;

import java.util.Collection;

/**
 * A registry for metadata providers.
 *
 * @see IMethod
 */
public interface IMethodRegistry {
	/**
	 * Register a method
	 *
	 * @param target The class this provider targets
	 * @param method The relevant method
	 */
	<T> void registerMethod(Class<T> target, IMethod<T> method);

	/**
	 * Get all methods for a context
	 *
	 * @param context The context to execute under
	 * @return List of valid methods
	 */
	<T> Collection<IMethod<T>> getMethods(IContext<T> context);

	/**
	 * Get all methods targeting a class
	 *
	 * @param target The class to invoke with
	 * @return List of valid methods
	 */
	Collection<IMethod<?>> getMethods(Class<?> target);

	/**
	 * Build a context for a target
	 *
	 * @param target The target to build for
	 * @return The build context
	 */
	<T> IContext<T> getContext(T target);

	/**
	 * Build a context for a target
	 *
	 * @param target The target to build for
	 * @param parent Parent context to copy from
	 * @return The build context
	 */
	<T> IContext<T> getContext(T target, IContext<?> parent);
}
