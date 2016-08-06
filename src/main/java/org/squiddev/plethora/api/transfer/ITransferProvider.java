package org.squiddev.plethora.api.transfer;

import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * A provider for a transfer location
 *
 * A "transfer location" is somewhere where objects can be transferred to or from. For instance you might target
 * {@link net.minecraft.entity.player.EntityPlayer} which could provide "inventory" and "ender_inventory".
 *
 * Transfer locations can also be chained together: each item is separated by ".". For instance "inventory.2" would
 * look-up "inventory" then lookup "2" on the resulting inventory object.
 */
public interface ITransferProvider<T> {
	/**
	 * @param object The object to get locations from
	 * @param key    The lookup for transfer locations
	 * @return The valid transfer location or {@code null} if none exists.
	 */
	@Nullable
	Object getTransferLocation(@Nonnull T object, @Nonnull String key);

	/**
	 * Get all primary transfer locations
	 *
	 * @param object The object to get locations from
	 * @return All valid locations. This can be empty for secondary providers.
	 */
	@Nonnull
	Set<String> getTransferLocations(@Nonnull T object);

	/**
	 * Automatically register a transfer provider.
	 *
	 * The class must have a public constructor and implement {@link IConverter}.
	 *
	 * @see ITransferRegistry#registerPrimary(Class, ITransferProvider)
	 * @see ITransferRegistry#registerSecondary(Class, ITransferProvider)
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

		/**
		 * Whether this converter is a primary converter
		 *
		 * @return If this is a primary converter
		 */
		boolean primary() default true;
	}
}
