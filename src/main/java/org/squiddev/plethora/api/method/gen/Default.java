package org.squiddev.plethora.api.method.gen;

import java.lang.annotation.*;

/**
 * Provide a default value for primitives.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {
	long defLong() default -1;

	int defInt() default -1;

	double defDoub() default -1;

	boolean defBool() default false;
}
