package org.squiddev.plethora.api.method;

import java.util.List;

/**
 * A special context object that is created if an object has any methods.
 *
 * This can be used to add special methods, such as documentation providers. An empty context
 * is created with this object as the target.
 */
public interface IMethodCollection {
	/**
	 * All gathered methods for this object
	 *
	 * @return A read only list of all gathered methods
	 */
	List<IMethod<?>> methods();

	/**
	 * See if any method implements an interface or class.
	 *
	 * This is used to see if a marker interface is present.
	 *
	 * @param iface The interface or class to check
	 * @return If any method implements this interface (or extends this class)
	 */
	boolean has(Class<?> iface);
}
