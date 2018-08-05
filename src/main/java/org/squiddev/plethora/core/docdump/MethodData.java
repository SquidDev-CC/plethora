package org.squiddev.plethora.core.docdump;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.ISubTargetedMethod;
import org.squiddev.plethora.api.module.IModuleMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Details about a method
 */
public class MethodData extends DocData<IMethod<?>> {
	private static final Pattern docString = Pattern.compile("^function(\\([^)]*\\).*?)--(.*)$");

	@Nullable
	public final String args;

	@Nullable
	public final String target;

	@Nullable
	public final String subtarget;

	@Nullable
	public final String[] modules;

	public MethodData(@Nonnull Class<?> target, @Nonnull IMethod<?> method) {
		super(method, method.getClass().getName(), method.getName(), getDescription(method.getDocString()));

		this.args = getArgs(method.getDocString());
		this.target = target.getName();
		this.subtarget = method instanceof ISubTargetedMethod
			? ((ISubTargetedMethod<?, ?>) method).getSubTarget().getName()
			: null;

		if (method instanceof IModuleMethod) {
			Set<ResourceLocation> modules = ((IModuleMethod<?>) method).getModules();
			this.modules = new String[modules.size()];

			int i = 0;
			for (ResourceLocation module : modules) {
				this.modules[i++] = module.toString();
			}

			Arrays.sort(this.modules);
		} else {
			modules = null;
		}
	}

	private static String getArgs(String doc) {
		if (doc == null) return null;

		Matcher match = docString.matcher(doc);
		return match.find() ? match.group(1).trim() : null;
	}

	private static String getDescription(String doc) {
		if (doc == null) return null;

		Matcher match = docString.matcher(doc);
		return match.find() ? match.group(2).trim() : null;
	}
}
