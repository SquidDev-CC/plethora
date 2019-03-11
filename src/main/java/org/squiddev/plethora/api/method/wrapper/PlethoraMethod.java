package org.squiddev.plethora.api.method.wrapper;

import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.IModuleMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which registers a generic method with Plethora.
 *
 * @see IMethod
 * @see ArgumentType
 * @see FromTarget
 * @see FromContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PlethoraMethod {
	/**
	 * Override the name(s) of this method. These are otherwise extracted from the method name.
	 *
	 * @return This method's names(s).
	 * @see IMethod#getName()
	 */
	String[] name() default {};

	/**
	 * The modules this function requires, or the empty list if none are required.
	 *
	 * @return This method's required module(s).
	 * @see IModuleMethod#getModules()
	 */
	String[] module() default {};

	/**
	 * Whether this method must be executed on the server thread.
	 *
	 * This should be left as true most of the time, unless you are certain that your context dependencies and the
	 * objects you are interacting with are thread-safe.
	 */
	boolean worldThread() default true;

	/**
	 * The documentation of this method.
	 *
	 * This should take the form {@code "function(arg1:string):table -- The summary of this method. And then any
	 * additional information"}. However, if your method signature is simple (does not take or return an opaque type
	 * like {@link org.squiddev.plethora.api.method.MethodResult} or {@link Object}) then we can automatically generate
	 * the signature for you - you only need to write {@code "-- The summary/information as above"}.
	 *
	 * @return This method's documentation.
	 * @see IMethod#getDocString()
	 */
	String doc();

	/**
	 * The mod id that this method depends on. We will not load or inject the method if it is not available.
	 *
	 * @return The dependent mod id, or an empty string if none is required.
	 */
	String modId() default "";
}
