package org.squiddev.plethora.api.method.wrapper;

import org.squiddev.plethora.api.method.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which registers a generic method with Plethora.
 *
 * {@link PlethoraMethod} provides a simple but powerful interface over {@link IMethod}, adding in support for requiring
 * types in the context, modules to be present, and automatic argument validation.
 *
 * Arguments to a method annotated by {@link PlethoraMethod} are processed as follows:
 * <ul>
 * <li>If the parameter has type {@link IPartialContext} or {@link IContext} then the type parameter will be taken as
 * the target, and the baked context will be injected when called.</li>
 * <li>If this parameter is annotated with {@link FromTarget}, its type will be used as the target and the value
 * injected using {@link IPartialContext#getTarget()}.</li>
 * <li>If this parameter is annotated with {@link FromContext}, we will attempt to inject it using
 * {@link IPartialContext#getContext(Class)}. If {@link FromContext#value()} is specified, we will only look within the
 * provided context keys instead. If the parameter is not marked as {@link Optional}, we will also require this
 * context item to be present (using {@link IMethod#canApply(IPartialContext)}.</li>
 * <li>If it is annotated with {@link FromSubtarget}, we will apply the same rules as for {@link FromContext}, but
 * defaulting to a context of {@link ContextKeys#ORIGIN}, followed by this method's modules.</li>
 * <li>Any other types are processed as Lua arguments. If this is a primitive or enum type, we will attempt to
 * parse it ourselves. Otherwise we will delegate to a suitable {@link ArgumentType}. Note, if a parameter is annotated
 * as {@link Optional}, we will default to {@code null}, or the specified default value for primitives.
 *
 * If you specify one argument of type {@link Object[]}, all arguments will be parsed as is.</li>
 * </ul>
 *
 * {@link PlethoraMethod} annotated methods may return a {@link MethodResult}, {@link Object[]}, {@link Object} or any
 * other value.
 *
 * @see IMethod
 * @see ArgumentType
 * @see FromTarget
 * @see FromContext
 * @see FromSubtarget
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PlethoraMethod {
	/**
	 * Override the name(s) of this method. These are otherwise extracted from the method name.
	 *
	 * @return This method's names(s).
	 * @see IMethod#getName()
	 */
	String[] name() default {};

	/**
	 * The modules this function requires, or the empty list if none are required.
	 *
	 * @return This method's required module(s).
	 * @see IMethod#getModules()
	 */
	String[] module() default {};

	/**
	 * Whether this method must be executed on the server thread.
	 *
	 * This should be left as true most of the time, unless you are certain that your context dependencies and the
	 * objects you are interacting with are thread-safe.
	 */
	boolean worldThread() default true;

	/**
	 * The documentation of this method.
	 *
	 * This should take the form {@code "function(arg1:string):table -- The summary of this method. And then any
	 * additional information"}. However, if your method signature is simple (does not take or return an opaque type
	 * like {@link MethodResult} or {@link Object}) then we can automatically generate
	 * the signature for you - you only need to write {@code "-- The summary/information as above"}.
	 *
	 * @return This method's documentation.
	 * @see IMethod#getDocString()
	 */
	String doc();

	/**
	 * The mod id that this method depends on. We will not load or inject the method if it is not available.
	 *
	 * @return The dependent mod id, or an empty string if none is required.
	 */
	String modId() default "";
}
