package org.squiddev.plethora.api.module;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * A top-level module method which requires a particular context object to execute.
 */
public abstract class SubtargetedModuleObjectMethod<T> extends ModuleContainerObjectMethod implements ISubTargetedMethod<IModuleContainer, T> {
	private final Class<T> klass;

	public SubtargetedModuleObjectMethod(String name, Set<ResourceLocation> modules, Class<T> klass, boolean worldThread) {
		this(name, modules, klass, worldThread, 0, null);
	}

	public SubtargetedModuleObjectMethod(String name, Set<ResourceLocation> modules, Class<T> klass, boolean worldThread, int priority) {
		this(name, modules, klass, worldThread, priority, null);
	}

	public SubtargetedModuleObjectMethod(String name, Set<ResourceLocation> modules, Class<T> klass, boolean worldThread, String docs) {
		this(name, modules, klass, worldThread, 0, docs);
	}

	public SubtargetedModuleObjectMethod(String name, Set<ResourceLocation> modules, Class<T> klass, boolean worldThread, int priority, String docs) {
		super(name, modules, worldThread, priority, docs);
		this.klass = klass;
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IModuleContainer> context) {
		if (!super.canApply(context)) return false;
		if (context.hasContext(ContextKeys.ORIGIN, klass)) return true;

		for (ResourceLocation module : getModules()) {
			if (context.hasContext(module.toString(), klass)) return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public Class<T> getSubTarget() {
		return klass;
	}

	@Nullable
	@Override
	public final Object[] apply(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		T object = context.getContext(ContextKeys.ORIGIN, klass);
		if (object == null) {
			for (ResourceLocation module : getModules()) {
				object = context.getContext(module.toString(), klass);
				if (object != null) break;
			}
		}

		return apply(object, context, args);
	}

	@Nullable
	public abstract Object[] apply(@Nonnull T target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException;

	/**
	 * Delegate to a normal method from a {@link SubtargetedModuleObjectMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link SubtargetedModuleObjectMethod#apply(Object, IContext, Object[])}.
	 * This does not allow fine grain control over whether a method can be applied or not. If you require
	 * {@link IMethod#canApply(IPartialContext)} you should use a normal {@link IMethod} instead.
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
		 * The modules this method requires.
		 *
		 * @return The target class.
		 */
		String[] module();

		/**
		 * The class this method targets
		 *
		 * @return The target class
		 */
		Class<?> target();

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
		 * Run this method on the world thread
		 *
		 * @return Whether this method should be run on the world thread
		 * @see BasicObjectMethod#BasicObjectMethod(String, boolean)
		 */
		boolean worldThread() default true;

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
