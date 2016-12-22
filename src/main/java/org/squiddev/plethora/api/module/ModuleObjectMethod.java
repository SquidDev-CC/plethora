package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * A method that requires a module to execute.
 */
public abstract class ModuleObjectMethod<T> extends BasicObjectMethod<T> implements IModuleMethod<T> {
	protected final Set<ResourceLocation> modules;

	public ModuleObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread) {
		this(name, modules, worldThread, 0, null);
	}

	public ModuleObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, int priority) {
		this(name, modules, worldThread, priority, null);
	}

	public ModuleObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, String docs) {
		this(name, modules, worldThread, 0, docs);
	}

	public ModuleObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, int priority, String docs) {
		super(name, worldThread, priority, docs);
		Preconditions.checkArgument(modules.size() > 0, "modules must be non-empty");
		this.modules = modules;
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<T> context) {
		if (!super.canApply(context)) return false;

		for (ResourceLocation module : modules) {
			if (!context.hasModule(module)) return false;
		}

		return true;
	}

	@Nonnull
	@Override
	public Set<ResourceLocation> getModules() {
		return modules;
	}

	/**
	 * Delegate to a normal method from a {@link ModuleMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link ModuleObjectMethod#apply(IContext, Object[])}.
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
		boolean worldThread();

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
