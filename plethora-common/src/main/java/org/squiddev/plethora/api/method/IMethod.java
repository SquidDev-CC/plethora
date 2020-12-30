package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.ModuleContainerMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * A Lua side method targeting a class.
 *
 * There are several ways of using {@link IMethod}:
 * - Extend {@link BasicMethod}, one of its subclasses or write your own. Then register using {@link Injects}.
 * - Create a static method on a class and register using {@link PlethoraMethod} or similar.
 */
public interface IMethod<T> {
	/**
	 * The name of this method
	 *
	 * @return The name of this method
	 */
	@Nonnull
	String getName();

	/**
	 * Get the doc string for this method.
	 *
	 * This can take several forms:
	 *
	 * - {@code Description of function}: A basic description with no types
	 * - {@code function(arg1:type [, optionArg:type]):returnType -- Description of function}: Function with return type
	 * - {@code function(arg1:type [, optionArg:type])->ret1:type [,optionRet1:type] -- Description of function}: Function with named return values
	 *
	 * Standard argument types are any, nil, string, number, integer, boolean and table.
	 *
	 * The function description can be multiple lines. The first line or sentence is read as a synopsis, with everything else being
	 * considered additional detail.
	 *
	 * @return The doc string.
	 */
	@Nonnull
	String getDocString();

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
	 * Check if this function can be applied in the given context.
	 *
	 * @param context The context to check in
	 * @return If this function can be applied.
	 * @see IContext#hasContext(Class)
	 */
	default boolean canApply(@Nonnull IPartialContext<T> context) {
		return true;
	}

	/**
	 * Apply the method
	 *
	 * @param context The context to apply within
	 * @param args    The arguments this function was called with
	 * @return The return values
	 * @throws LuaException     On the event of an error
	 * @throws RuntimeException Unhandled errors: these will be rethrown as {@link LuaException}s and the call stack logged.
	 * @see dan200.computercraft.api.lua.ILuaObject#callMethod(ILuaContext, int, Object[])
	 */
	@Nonnull
	MethodResult apply(@Nonnull IUnbakedContext<T> context, @Nonnull Object[] args) throws LuaException;

	/**
	 * See if this method implements an interface or class.
	 *
	 * This is used to see if a marker interface is present.
	 *
	 * @param iface The interface or class to check
	 * @return If any method implements this interface (or extends this class)
	 * @see IMethodCollection#has(Class)
	 */
	default boolean has(@Nonnull Class<?> iface) {
		return iface.isInstance(this);
	}

	/**
	 * Get the modules that this method requires.
	 *
	 * Note, this does not in and of itself impose any additional functionality; this only exists for documentation
	 * purposes. You should override {@link #canApply(IPartialContext)} or use a existing class like
	 * {@link ModuleContainerMethod} to actually enforce constraints.
	 *
	 * @return The modules that this method requires
	 */
	@Nonnull
	default Collection<ResourceLocation> getModules() {
		return Collections.emptySet();
	}

	/**
	 * Get the sub-target for this method.
	 *
	 * For instance objects which reference particular {@link net.minecraft.item.Item} classes
	 * will target {@link net.minecraft.item.ItemStack} instead.
	 *
	 * This does not have any actual functionality (use {@link SubtargetedModuleMethod}) for that, it only exists for
	 * documentation purposes.
	 *
	 * @return The method's sub-target, or {@code null} if we have no sub-target.
	 */
	@Nullable
	default Class<?> getSubTarget() {
		return null;
	}

	/**
	 * A delegate for some {@link IMethod}.
	 *
	 * @param <T> The type of this delegate's target.
	 */
	interface Delegate<T> {
		/**
		 * Apply this method delegate method
		 *
		 * @param context The context to apply within
		 * @param args    The arguments this function was called with
		 * @return The return values
		 * @throws LuaException     On the event of an error
		 * @throws RuntimeException Unhandled errors: these will be rethrown as {@link LuaException}s and the call stack logged.
		 * @see IMethod#apply(IUnbakedContext, Object[])
		 */
		MethodResult apply(IUnbakedContext<T> context, Object[] args) throws LuaException;
	}
}
