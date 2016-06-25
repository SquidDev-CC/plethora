package org.squiddev.plethora.api.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically register a converter
 * The class must have a public constructor and implement {@link IConverter}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Converter {
	/**
	 * The target class
	 *
	 * @return The target class
	 */
	Class<?> value();
}
