package org.squiddev.plethora.api.method.wrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;
import java.lang.annotation.*;

/**
 * Provide a default value for primitives.
 */
@Documented
@Nullable
@TypeQualifierNickname
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Nonnull(when = When.UNKNOWN)
public @interface Optional {
	long defLong() default -1;

	int defInt() default -1;

	double defDoub() default -1;

	boolean defBool() default false;
}
