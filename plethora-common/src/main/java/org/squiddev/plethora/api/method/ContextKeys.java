package org.squiddev.plethora.api.method;

/**
 * A collection of built-in context keys.
 *
 * All objects in the context under a given key are associated with the current
 * target in a specific way. For instance, {@link #ORIGIN} marks objects from which
 * this method call originates (such as the peripheral or computer).
 *
 * If you have module-specific information you wish to store in the context, it is
 * recommended you place it under a key of the same name.
 *
 * @see IPartialContext#hasContext(String, Class)
 * @see IPartialContext#getContext(String, Class)
 */
public final class ContextKeys {
	private ContextKeys() {
	}

	/**
	 * The active target for this context.
	 *
	 * This is generally composed of one primary object and several converted objects.
	 *
	 * @see IPartialContext#getTarget()
	 */
	public static final String TARGET = "target";

	/**
	 * The place where this context first originates. This may be used in order to perform
	 * tasks relative to the base, without contaminating the context with more general properties.
	 */
	public static final String ORIGIN = "origin";

	/**
	 * This is a dumping ground for any context objects which do not belong elsewhere. When forming a
	 * child context, any parent targets will be placed in the generic key instead.
	 */
	public static final String GENERIC = "generic";

	/**
	 * Any computer/peripheral specific information. For instance,
	 * {@link dan200.computercraft.api.peripheral.IComputerAccess}.
	 */
	public static final String COMPUTER = "computer";
}
