package org.squiddev.plethora.api.method;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A basic wrapper for methods
 */
public abstract class BasicMethod<T> implements IMethod<T> {
	private final String name;
	private final String docs;
	private final int priority;

	public BasicMethod(String name) {
		this(name, 0, null);
	}

	public BasicMethod(String name, int priority) {
		this(name, priority, null);
	}

	public BasicMethod(String name, String docs) {
		this(name, 0, docs);
	}

	public BasicMethod(String name, int priority, String docs) {
		this.name = name;
		this.priority = priority;
		this.docs = Strings.isNullOrEmpty(docs) ? null : docs;
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public boolean canApply(@Nonnull IContext<T> context) {
		return true;
	}

	@Override
	public final int getPriority() {
		return priority;
	}

	@Nullable
	@Override
	public String getDocString() {
		return docs;
	}


	/**
	 * Delegate to a normal method from a {@link BasicMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link BasicMethod#apply(IUnbakedContext, Object[])}.
	 * This does not allow fine grain control over whether a method can be applied or not. If you require
	 * {@link IMethod#canApply(IContext)} you should use a normal {@link IMethod} instead.
	 *
	 * Use {@link #modId()} instead of {@link net.minecraftforge.fml.common.Optional.Method} if you require a mod to
	 * be loaded. This allows us to blacklist mods in the config.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Inject {
		/**
		 * The name this function should be exposed as.
		 *
		 * This defaults to the method's name
		 *
		 * @return The function's name
		 * @see IMethod#getName()
		 */
		String name() default "";

		/**
		 * The class this method targets.
		 *
		 * @return The target class.
		 */
		Class<?> value();

		/**
		 * The priority of the method.
		 *
		 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Methods
		 * with higher priorities will be preferred.
		 *
		 * @return The method's priority
		 * @see IMethod#getPriority()
		 */
		int priority() default 0;

		/**
		 * The method's doc string.
		 *
		 * See {@link IMethod#getDocString()} for format information
		 *
		 * @return The method's doc string
		 * @see IMethod#getDocString()
		 */
		String doc() default "";

		/**
		 * Set if this method depends on a mod
		 *
		 * @return The mod's id
		 * @see net.minecraftforge.fml.common.Optional.Method
		 * @see net.minecraftforge.fml.common.Optional.Interface
		 */
		String modId() default "";
	}
}
