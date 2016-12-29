package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * A method that requires a module to execute.
 */
public abstract class ModuleContainerMethod extends BasicMethod<IModuleContainer> implements IModuleMethod<IModuleContainer> {
	protected final Set<ResourceLocation> modules;

	public ModuleContainerMethod(String name, Set<ResourceLocation> modules) {
		this(name, modules, 0, null);
	}

	public ModuleContainerMethod(String name, Set<ResourceLocation> modules, int priority) {
		this(name, modules, priority, null);
	}

	public ModuleContainerMethod(String name, Set<ResourceLocation> modules, String doc) {
		this(name, modules, 0, doc);
	}

	public ModuleContainerMethod(String name, Set<ResourceLocation> modules, int priority, String doc) {
		super(name, priority, doc);
		Preconditions.checkArgument(modules.size() > 0, "modules must be non-empty");
		this.modules = modules;
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IModuleContainer> context) {
		if (!super.canApply(context)) return false;

		IModuleContainer container = context.getTarget();
		for (ResourceLocation module : modules) {
			if (!container.hasModule(module)) return false;
		}

		return true;
	}

	@Nonnull
	@Override
	public Set<ResourceLocation> getModules() {
		return modules;
	}

	/**
	 * Delegate to a normal method from a {@link ModuleContainerMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link ModuleContainerMethod#apply(IUnbakedContext, Object[])}.
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
		 * @see ModuleContainerMethod#ModuleContainerMethod(String, Set)
		 */
		String[] value();

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
