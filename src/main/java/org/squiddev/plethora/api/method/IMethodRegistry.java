package org.squiddev.plethora.api.method;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A registry for metadata providers.
 *
 * @see IMethod
 */
public interface IMethodRegistry {
	/**
	 * Get all methods for a context
	 *
	 * @param context The context to execute under
	 * @return List of valid methods
	 */
	@Nonnull
	<T> List<IMethod<T>> getMethods(@Nonnull IPartialContext<T> context);

	/**
	 * Get all methods targeting a class
	 *
	 * @param target The class to invoke with
	 * @return List of valid methods
	 */
	@Nonnull
	List<IMethod<?>> getMethods(@Nonnull Class<?> target);

	/**
	 * Get the cost handler for this object
	 *
	 * @param object The cost handler's owner
	 * @param side   The side to get the cost handler from
	 * @return The associated cost handler
	 */
	@Nonnull
	ICostHandler getCostHandler(@Nonnull ICapabilityProvider object, @Nullable EnumFacing side);

	/**
	 * Gets the base cost of a method. This is subtracted when the method is executed.
	 *
	 * @param method The method to get the base cost of.
	 * @return The base cost of the method.
	 * @see ICostHandler
	 */
	int getBaseMethodCost(IMethod<?> method);
}
