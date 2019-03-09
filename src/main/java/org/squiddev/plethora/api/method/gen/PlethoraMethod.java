package org.squiddev.plethora.api.method.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PlethoraMethod {
	/**
	 * Override the name(s) of this method. These are otherwise extracted from the method name.
	 *
	 * @return This method's names(s).
	 */
	String[] name() default {""};

	/**
	 * The modules this function requires, or the empty list if none are required.
	 *
	 * @return This method's required module(s).
	 */
	String[] module() default {""};

	/**
	 * Whether this method must be executed on the world thread.
	 */
	boolean worldThread() default true;

	/**
	 * The documentation of this method
	 *
	 * @return This method's documented
	 */
	String doc() default "";

	String modId() default "";
}
