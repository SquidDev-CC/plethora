package org.squiddev.plethora.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a class into the various Plethora registries.
 *
 * This may either be used on an implementation of a registerable interface, or on a class with several public static
 * fields whose values should be registered.
 *
 * @see org.squiddev.plethora.api.transfer.ITransferProvider
 * @see org.squiddev.plethora.api.converter.IConverter
 * @see org.squiddev.plethora.api.method.IMethodBuilder
 * @see org.squiddev.plethora.api.method.IMethod
 * @see org.squiddev.plethora.api.meta.IMetaProvider
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Injects {
	/**
	 * The mod id that this injection registry depends on. This class will not be loaded, and so the dependencies not
	 * registered, if it is not specified.
	 *
	 * @return The dependent mod id, or empty if none is required.
	 */
	String value() default "";
}
