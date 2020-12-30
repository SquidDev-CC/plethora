package org.squiddev.plethora.core.docdump;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.core.RegisteredMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Details about a method
 */
public class DocumentedMethod extends DocumentedItem<RegisteredMethod<?>> {
	private static final Pattern docString = Pattern.compile("^function(\\([^)]*\\).*?)--(.*)$");

	private final String args;
	private final Class<?> target;
	private final Class<?> subtarget;
	private final List<String> modules;

	public DocumentedMethod(@Nonnull RegisteredMethod<?> method) {
		super(method, method.name(), method.method().getName(), getDescription(method.method().getDocString()));

		target = method.target();
		args = getArgs(getDetail());
		subtarget = method.method().getSubTarget();

		Collection<ResourceLocation> modules = method.method().getModules();
		if (modules.isEmpty()) {
			this.modules = Collections.emptyList();
		} else {
			List<String> moduleList = new ArrayList<>(modules.size());
			for (ResourceLocation module : modules) moduleList.add(module.toString());
			moduleList.sort(Comparator.naturalOrder());
			this.modules = Collections.unmodifiableList(moduleList);
		}
	}

	/**
	 * The argument list for this method
	 *
	 * @return This method's argument list, or {@code "()"} if none is available.
	 */
	@Nonnull
	public String getArgs() {
		return args == null ? "()" : args;
	}

	/**
	 * The class this method targets
	 *
	 * @return This method 's target
	 */
	@Nonnull
	public Class<?> getTarget() {
		return target;
	}

	/**
	 * The subtarget for this method
	 *
	 * @return This method's subtarget, or {@code null} if none is available.
	 * @see ISubTargetedMethod
	 */
	@Nullable
	public Class<?> getSubtarget() {
		return subtarget;
	}

	/**
	 * All modules this method depends on
	 *
	 * @return This method's modules
	 */
	@Nonnull
	public List<String> getModules() {
		return modules;
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
