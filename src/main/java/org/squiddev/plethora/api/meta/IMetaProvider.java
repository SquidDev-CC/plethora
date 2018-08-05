package org.squiddev.plethora.api.meta;

import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Provides metadata about an object
 * Register with {@link IMetaRegistry}
 */
public interface IMetaProvider<T> {
	/**
	 * Get metadata about an object
	 *
	 * @param context The object to get metadata about
	 * @return The gathered data. Do not return {@code null}.
	 */
	@Nonnull
	Map<Object, Object> getMeta(@Nonnull IPartialContext<T> context);

	/**
	 * Get the priority of this provider
	 *
	 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Providers
	 * with higher priorities will be preferred.
	 *
	 * @return The provider's priority
	 */
	default int getPriority() {
		return 0;
	}

	/**
	 * Get a basic description of this meta provider
	 *
	 * @return This provider's description, or {@code null} if none is available.
	 */
	@Nullable
	default String getDescription() {
		return null;
	}

	/**
	 * Get an example input for this meta provider
	 *
	 * @return An example input for this meta provider, or {@code null} if none is available.
	 */
	@Nullable
	default T getExample() {
		return null;
	}

	/**
	 * Automatically register a meta provider.
	 *
	 * The class must have a public constructor and implement {@link IMetaProvider}.
	 *
	 * @see IMetaRegistry#registerMetaProvider(Class, IMetaProvider)
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	@interface Inject {
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

		/**
		 * Set if this meta provider depends on a mod
		 *
		 * @return The mod's id
		 * @see net.minecraftforge.fml.common.Optional.Method
		 * @see net.minecraftforge.fml.common.Optional.Interface
		 */
		String modId() default "";
	}
}
