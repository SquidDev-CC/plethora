package org.squiddev.plethora.api.method;

import javax.annotation.Nonnull;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A method to {@link IMethod} instance converters.
 *
 * @see MethodBuilder
 * @see IMethodRegistry#registerMethodBuilder(Class, IMethodBuilder)
 */
public interface IMethodBuilder<T extends Annotation> {
	/**
	 * Write the constructor for this method.
	 *
	 * You should generate an 0 argument constructor and all other required methods (such as delegating to the
	 * actual method).
	 *
	 * @param method     The method being generated
	 * @param annotation The annotation data for this method
	 * @param name       The internal name of the class to generate
	 */
	@Nonnull
	byte[] writeClass(@Nonnull Method method, @Nonnull T annotation, @Nonnull String name);

	/**
	 * Validate the method
	 *
	 * @param method     The method being generated
	 * @param annotation The annotation data for this method
	 * @return A list of errors encountered
	 */
	@Nonnull
	List<String> validate(@Nonnull Method method, @Nonnull T annotation);

	/**
	 * Get the target for this method
	 *
	 * @param method     The method being generated
	 * @param annotation The annotation data for this method
	 * @return The method's target
	 * @see IMethod.Inject#value()
	 * @see IMethodRegistry#registerMethod(Class, IMethod)
	 */
	Class<?> getTarget(@Nonnull Method method, @Nonnull T annotation);

	/**
	 * Automatically register a {@link IMethodBuilder}
	 * The class must have a public constructor and implement {@link IMethodBuilder}
	 *
	 * @see IMethodRegistry#registerMethod(Class, IMethod)
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	@interface Inject {
		/**
		 * The target annotation
		 *
		 * @return The target annotation
		 */
		Class<? extends Annotation> value();
	}
}
