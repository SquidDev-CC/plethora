package org.squiddev.plethora.api.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically register a method
 * The class must have a public constructor and implement {@link IMethod}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Method {
	/**
	 * The target class
	 *
	 * @return The target class
	 */
	Class<?> value();

	/**
	 * Set if this method depends on a mod
	 *
	 * @return The mod's id
	 * @see net.minecraftforge.fml.common.Optional.Method
	 * @see net.minecraftforge.fml.common.Optional.Interface
	 */
	String modId() default "";
}
