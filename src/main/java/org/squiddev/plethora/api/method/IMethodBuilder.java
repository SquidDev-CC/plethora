package org.squiddev.plethora.api.method;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * A method to {@link IMethod} instance converters.
 *
 * @param <T> The annotation to target. This must have a modId field.
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
	 * @param method           The method being generated
	 * @param annotation       The annotation data for this method
	 * @param markerInterfaces Interfaces which should be added to this class
	 * @param name             The internal name of the class to generate
	 */
	@Nonnull
	byte[] writeClass(@Nonnull Method method, @Nonnull T annotation, @Nonnull Set<Class<?>> markerInterfaces, @Nonnull String name);

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
	 */
	Class<?> getTarget(@Nonnull Method method, @Nonnull T annotation);
}
