package org.squiddev.plethora.api.converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An implicit converter: takes an object and converts it into something else
 */
public interface IConverter<TIn, TOut> {
	/**
	 * Convert an object from one object into another.
	 * Used to provide additional objects from one
	 *
	 * @param from The object to convert from
	 * @return The converted object
	 */
	@Nullable
	TOut convert(@Nonnull TIn from);

	/**
	 * Whether this converter will always return the same object for a given
	 * input.
	 *
	 * Note, you may make some assumptions about the conversion,
	 * such that the block and tile will not change.
	 *
	 * @return If this converter is constant.
	 * @see ConstantConverter
	 * @see DynamicConverter
	 */
	boolean isConstant();

	/**
	 * Automatically register a converter.
	 *
	 * The class must have a public constructor and implement {@link IConverter}.
	 *
	 * @see IConverterRegistry#registerConverter(Class, IConverter)
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	@interface Inject {
		/**
		 * The target class
		 *
		 * @return The target class
		 */
		Class<?> value();

		/**
		 * Set if this converter depends on a mod
		 *
		 * @return The mod's id
		 * @see net.minecraftforge.fml.common.Optional.Method
		 * @see net.minecraftforge.fml.common.Optional.Interface
		 */
		String modId() default "";
	}
}
