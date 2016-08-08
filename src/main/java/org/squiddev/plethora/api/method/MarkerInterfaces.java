package org.squiddev.plethora.api.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A list of marker interfaces which should be added to the method.
 *
 * This should be used on methods injected through builders.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MarkerInterfaces {
	Class<?>[] value();
}
