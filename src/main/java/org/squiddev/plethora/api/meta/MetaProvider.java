package org.squiddev.plethora.api.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically register a meta provider
 * The class must have a public constructor and implement {@link IMetaProvider}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MetaProvider {
	/**
	 * The target to apply to
	 *
	 * @return The target to apply to
	 */
	Class<?> value();

	/**
	 * The namespace to insert into.
	 *
	 * @return The namespace to insert into. None if empty string or {@code null}.
	 */
	String namespace() default "";
}
