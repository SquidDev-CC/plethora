package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;

import java.util.List;

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
	<T> List<IMethod<T>> getMethods(IContext<T> context);

	/**
	 * Get all methods targeting a class
	 *
	 * @param target The class to invoke with
	 * @return List of valid methods
	 */
	List<IMethod<?>> getMethods(Class<?> target);

	/**
	 * Get a lua object for an object
	 *
	 * @param context The context to execute under
	 * @return The build Lua object
	 */
	<T> ILuaObject getObject(IUnbakedContext<T> context);
}
