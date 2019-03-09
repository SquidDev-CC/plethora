package org.squiddev.plethora.core.gen;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.MarkerInterfaces;
import org.squiddev.plethora.api.method.gen.FromContext;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.core.gen.MethodInstance.ContextInfo;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import static org.squiddev.plethora.core.PlethoraCore.LOG;

public final class PlethoraMethodRegistry {
	private static final Type PARTIAL_CONTEXT_T = IPartialContext.class.getTypeParameters()[0];

	static boolean add(Method method) {
		PlethoraMethod annotation = method.getAnnotation(PlethoraMethod.class);
		String name = method.getDeclaringClass().getName() + "." + method.getName();
		if (annotation == null) {
			PlethoraCore.LOG.error("@PlethoraMethod method {} is not actually annotated", name);
			return false;
		}

		boolean ok = true;

		// Ensure we have permissions to call this method
		int modifiers = method.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			PlethoraCore.LOG.error("@PlethoraMethod method {} should be public static, but is {}.", name, Modifier.toString(modifiers));
			return false;
		}

		// Extract some trivial properties
		String[] names = annotation.name();
		String docs = annotation.doc();
		String[] moduleNames = annotation.module();
		ResourceLocation[] modules;
		if (names.length == 0 || names.length == 1 && names[0].equals("")) names = new String[]{method.getName()};
		if (Strings.isNullOrEmpty(docs)) docs = null;
		if (moduleNames.length == 0 || moduleNames.length == 1 && moduleNames[0].equals("")) {
			modules = null;
		} else {
			modules = new ResourceLocation[moduleNames.length];
			for (int i = 0; i < moduleNames.length; i++) modules[i] = new ResourceLocation(moduleNames[i]);
		}

		MarkerInterfaces markers = method.getAnnotation(MarkerInterfaces.class);
		Class<?>[] markerIfaces = markers == null || markers.value().length == 0 ? null : markers.value();

		// Extract our required context and validate the arguments
		Class<?> target = null;
		List<ContextInfo> context = new ArrayList<>();
		Parameter[] parameters = method.getParameters();

		// First scan all "context" arguments.
		int contextIndex;
		for (contextIndex = 0; contextIndex < parameters.length; contextIndex++) {
			Parameter parameter = parameters[contextIndex];
			FromContext fromContext = parameter.getAnnotation(FromContext.class);
			FromTarget fromTarget = parameter.getAnnotation(FromTarget.class);
			boolean contextTarget = parameter.getType() == IContext.class || parameter.getType() == IPartialContext.class;

			// TODO: Determine if we're IPartialContext or IContext - read this as @FromTarget and verify it's not
			//  annotated. Also do codegen for that.

			if (fromContext == null && fromTarget == null && !contextTarget) break;

			if (fromContext != null && fromTarget != null) {
				PlethoraCore.LOG.error(
					"@PlethoraMethod method {}'s has a context argument {} with both @FromContext and @FromTarget",
					name, parameter.getName(), parameter.getType().getName()
				);
				ok = false;
			}

			if (contextTarget && (fromTarget != null || fromContext != null)) {
				PlethoraCore.LOG.error(
					"@PlethoraMethod method {}'s has a context argument {} which is both annotated and has a {} type.",
					name, parameter.getName(), parameter.getType().getName()
				);
				ok = false;
			}

			if (parameter.getAnnotation(Nullable.class) != null) {
				if (fromTarget != null || contextTarget) {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s target has a nullable context argument {}.", name, parameter.getName());
					ok = false;
				}
				continue;
			}

			if (parameter.getClass().isPrimitive()) {
				PlethoraCore.LOG.error(
					"@PlethoraMethod method {}'s has a context argument {} with a primitive type {}",
					name, parameter.getName(), parameter.getType().getName()
				);
				ok = false;
			}

			if (contextTarget) {
				Type typeParameter = TypeToken.of(parameter.getParameterizedType()).resolveType(PARTIAL_CONTEXT_T).getType();
				Class<?> rawType = getRawType(parameter, typeParameter);
				if (rawType == null) {
					ok = false;
				} else if (target == null) {
					target = rawType;
				} else {
					PlethoraCore.LOG.error(
						"@PlethoraMethod method {}'s has multiple targets ({} and {}).",
						name, parameter.getName(), parameter.getType().getName()
					);
					ok = false;
				}
			} else if (fromContext == null) {
				if (getRawType(parameter) == null) ok = false;

				if (target == null) {
					target = parameter.getType();
				} else {
					PlethoraCore.LOG.error(
						"@PlethoraMethod method {}'s has multiple targets ({} and {}).",
						name, parameter.getName(), parameter.getType().getName()
					);
					ok = false;
				}
			} else {
				if (getRawType(parameter) == null) ok = false;

				String[] keys = fromContext.value();
				if (keys.length == 0 || keys.length == 1 && keys[0].equals("")) keys = null;
				context.add(new ContextInfo(keys, parameter.getType()));
			}
		}

		// Some basic validation of Lua arguments.
		for (int i = contextIndex; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if (parameter.getAnnotation(FromContext.class) != null || parameter.getAnnotation(FromTarget.class) != null) {
				LOG.error("@PlethoraMethod {} has a context annotation {} after a Lua argument", name, parameter.getName());
				ok = false;
			}

			if (getRawType(parameter) == null) ok = false;
		}

		if (target == null) {
			LOG.error("@PlethoraMethod {} has no obvious target.", name);
			ok = false;
		}

		if (!ok) return false;

		//noinspection unchecked
		MethodRegistry.instance.registerMethod(target, new MethodInstance(
			method, names, docs, annotation.worldThread(),
			context.toArray(new ContextInfo[0]), contextIndex,
			modules,
			markerIfaces));
		return true;
	}

	private static Class<?> getRawType(Parameter parameter) {
		return getRawType(parameter, parameter.getParameterizedType());
	}

	private static Class<?> getRawType(Parameter parameter, Type underlying) {
		while (true) {
			if (underlying instanceof Class<?>) return (Class<?>) underlying;

			if (underlying instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) underlying;
				for (Type arg : type.getActualTypeArguments()) {
					if (arg instanceof WildcardType) continue;
					if (arg instanceof TypeVariable && ((TypeVariable) arg).getName().startsWith("capture#")) continue;

					LOG.error("@PlethoraMethod {}.{} argument {} has generic type {} with non-wildcard argument {}.",
						parameter.getDeclaringExecutable().getDeclaringClass().getName(), parameter.getDeclaringExecutable().getName(),
						parameter.getName(), parameter.getParameterizedType(), arg
					);
					return null;
				}

				// Continue to extract from this child
				underlying = type.getRawType();
			} else {
				LOG.error("@PlethoraMethod {}.{} argument {} has unknown type {}.",
					parameter.getDeclaringExecutable().getDeclaringClass().getName(), parameter.getDeclaringExecutable().getName(),
					parameter.getName(), parameter.getParameterizedType()
				);
				return null;
			}
		}
	}

	public static void loadAsm(ASMDataTable asmDataTable) {
		boolean ok = true;
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(PlethoraMethod.class.getName())) {
			String className = asmData.getClassName();
			String methodWhole = asmData.getObjectName();

			try {
				if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, className + "#" + methodWhole)) {
					PlethoraCore.LOG.debug("Ignoring " + className + "#" + methodWhole);
					continue;
				}

				String modName = (String) asmData.getAnnotationInfo().get("modId");
				if (!Strings.isNullOrEmpty(modName) && !Helpers.modLoaded(modName)) {
					PlethoraCore.LOG.debug("Skipping " + className + "#" + methodWhole + " as " + modName + " is not loaded or is blacklisted");
					continue;
				}

				Class<?> klass = Class.forName(className);
				Method method = findMethod(methodWhole, klass);

				if (method == null) {
					PlethoraCore.LOG.warn("Cannot find method" + className + "#" + methodWhole + ".");
					continue;
				}

				PlethoraCore.LOG.debug("Registering " + className + "#" + methodWhole);
				ok &= add(method);
			} catch (Throwable e) {
				PlethoraCore.LOG.error("Failed to load: " + className + "#" + methodWhole, e);
				ok = false;
			}
		}

		if (!ok && ConfigCore.Testing.strict) {
			throw new IllegalStateException("Errors occurred when processing @PlethoraMethod annotations. See the log above for more details.");
		}
	}

	private static Method findMethod(String methodWhole, Class<?> klass) {
		int offset = methodWhole.indexOf('(');
		String methodName = methodWhole.substring(0, offset);
		String methodDesc = methodWhole.substring(offset);

		for (Method method : klass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				if (org.objectweb.asm.Type.getMethodDescriptor(method).equals(methodDesc)) {
					return method;
				}
			}
		}

		return null;
	}
}
