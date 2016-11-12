package org.squiddev.plethora.api.method;

import com.google.common.collect.Multimap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * A registry for metadata providers.
 *
 * @see IMethod
 */
public interface IMethodRegistry {
	/**
	 * Register a method
	 *
	 * @param target The class this provider targets
	 * @param method The relevant method
	 */
	<T> void registerMethod(@Nonnull Class<T> target, @Nonnull IMethod<T> method);

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
	 * Group of all methods available
	 *
	 * @return All methods, grouped by their target class
	 */
	@Nonnull
	Multimap<Class<?>, IMethod<?>> getMethods();

	/**
	 * Build a context
	 *
	 * @param target  The object to target
	 * @param handler The cost handler for this object
	 * @param modules A reference which will all modules for this context. This must return a constant value.
	 * @param context Additional context items
	 * @return The built context
	 */
	@Nonnull
	<T> IUnbakedContext<T> makeContext(@Nonnull IReference<T> target, @Nonnull ICostHandler handler, @Nonnull IReference<Set<ResourceLocation>> modules, @Nonnull IReference<?>... context);

	/**
	 * Get the cost handler for this object
	 *
	 * @param object The cost handler's owner
	 * @return The associated cost handler
	 */
	@Nonnull
	ICostHandler getCostHandler(@Nonnull ICapabilityProvider object);

	/**
	 * Register a method builder
	 *
	 * @param klass   The annotation to build from
	 * @param builder The builder to register
	 */
	<T extends Annotation> void registerMethodBuilder(@Nonnull Class<T> klass, @Nonnull IMethodBuilder<T> builder);

	/**
	 * Gets the base cost of a method. This is subtracted when the method is executed.
	 *
	 * @param method The method to get the base cost of.
	 * @return The base cost of the method.
	 * @see ICostHandler
	 */
	int getBaseMethodCost(IMethod method);
}
