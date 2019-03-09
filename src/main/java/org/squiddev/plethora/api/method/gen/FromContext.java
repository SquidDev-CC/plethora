package org.squiddev.plethora.api.method.gen;

import java.lang.annotation.*;

/**
 * Extract this value from the context, rather than taking it as an argument.
 *
 * One can use {@link javax.annotation.Nullable} in order to mark this as optional.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FromContext {
	/**
	 * The context key to extract from. Leave blank to take from any of them
	 *
	 * @return The context key to use.
	 */
	String[] value() default {""};
}
