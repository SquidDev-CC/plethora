package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method bound to a specific module
 */
public abstract class ModuleMethod extends BasicMethod<IModule> implements IModuleMethod {
	protected final ResourceLocation module;

	public ModuleMethod(String name, ResourceLocation module) {
		this(name, module, 0, null);
	}

	public ModuleMethod(String name, ResourceLocation module, int priority) {
		this(name, module, priority, null);
	}

	public ModuleMethod(String name, ResourceLocation module, String doc) {
		this(name, module, 0, doc);
	}

	public ModuleMethod(String name, ResourceLocation module, int priority, String doc) {
		super(name, priority, doc);
		this.module = module;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.getTarget().getModuleId().equals(module);
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return module;
	}

	/**
	 * Delegate to a normal method from a {@link ModuleMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link ModuleMethod#apply(IUnbakedContext, Object[])}.
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
		 * The module this method targets.
		 *
		 * @return The target class.
		 * @see ModuleMethod#ModuleMethod(String, ResourceLocation)
		 */
		String value();

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
