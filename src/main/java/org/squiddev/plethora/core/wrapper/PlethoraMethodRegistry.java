package org.squiddev.plethora.core.wrapper;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import dan200.computercraft.api.lua.ILuaObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.method.wrapper.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.core.RegisteredMethod;
import org.squiddev.plethora.core.wrapper.MethodInstance.ContextInfo;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.squiddev.plethora.core.PlethoraCore.LOG;

public final class PlethoraMethodRegistry {
	private static final String[] ORIGIN = new String[]{ ContextKeys.ORIGIN };

	private static final Type PARTIAL_CONTEXT_T = IPartialContext.class.getTypeParameters()[0];

	private PlethoraMethodRegistry() {
	}

	static boolean add(Method method, String modId) {
		PlethoraMethod annotation = method.getAnnotation(PlethoraMethod.class);
		String name = method.getDeclaringClass().getName() + "." + method.getName();
		if (annotation == null) {
			PlethoraCore.LOG.error("@PlethoraMethod method {} is not actually annotated", name);
			return false;
		}

		// Ensure we have permissions to call this method
		int modifiers = method.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			PlethoraCore.LOG.error("@PlethoraMethod method {} should be public static, but is {}.", name, Modifier.toString(modifiers));
			return false;
		}

		// Extract our module names.
		String[] moduleNames = annotation.module();
		ResourceLocation[] modules;
		if (moduleNames.length == 0) {
			moduleNames = null;
			modules = null;
		} else {
			modules = new ResourceLocation[moduleNames.length];
			for (int i = 0; i < moduleNames.length; i++) modules[i] = new ResourceLocation(moduleNames[i]);
		}

		// Extract our required context and validate the arguments
		Class<?> target = null, subTarget = null;
		List<ContextInfo> context = new ArrayList<>();
		Parameter[] parameters = method.getParameters();

		// First scan all "context" arguments. Look, I'm sorry - this is ugly as anything.
		int contextIndex;
		boolean ok = true;
		for (contextIndex = 0; contextIndex < parameters.length; contextIndex++) {
			Parameter parameter = parameters[contextIndex];
			FromContext fromContext = parameter.getAnnotation(FromContext.class);
			FromTarget fromTarget = parameter.getAnnotation(FromTarget.class);
			FromSubtarget fromSubtarget = parameter.getAnnotation(FromSubtarget.class);
			boolean contextTarget = parameter.getType() == IContext.class || parameter.getType() == IPartialContext.class;

			int counts = 0;
			if (fromContext != null) counts++;
			if (fromTarget != null) counts++;
			if (fromSubtarget != null) counts++;
			if (contextTarget) counts++;

			if (counts == 0) break;
			if (counts > 1) {
				PlethoraCore.LOG.error(
					"@PlethoraMethod method {}'s has a context argument {} with multiple annotations",
					name, parameter.getName(), parameter.getType().getName()
				);
				ok = false;
			}

			// We don't need to require Optional annotations. However, targets/sub-targets shouldn't be marked as
			// optional.
			if (parameter.getAnnotation(Optional.class) != null) {
				if (fromTarget != null || contextTarget) {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s target has an @Optional context argument {}.", name, parameter.getName());
					ok = false;
				} else if (fromSubtarget != null) {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s sub-target has an @Optional context argument {}.", name, parameter.getName());
					ok = false;
				}
				continue;
			}

			// What on earth were they even trying to do here?
			if (parameter.getClass().isPrimitive()) {
				PlethoraCore.LOG.error(
					"@PlethoraMethod method {}'s has a context argument {} with a primitive type {}",
					name, parameter.getName(), parameter.getType().getName()
				);
				ok = false;
			}

			if (contextTarget) {
				// We need to extract the T from IPartialContext<T>, and mark that as our target.
				Type typeParameter = TypeToken.of(parameter.getParameterizedType()).resolveType(PARTIAL_CONTEXT_T).getType();
				Class<?> rawType = getRawType(parameter, typeParameter);
				if (rawType == null) {
					ok = false;
				} else if (target == null) {
					target = rawType;
				} else {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s has multiple targets.", name);
					ok = false;
				}
			} else if (fromTarget != null) {
				// Ensure the argument is raw, and set it as our target.
				if (getRawType(parameter) == null) ok = false;

				if (target == null) {
					target = parameter.getType();
				} else {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s has multiple targets.", name);
					ok = false;
				}
			} else if (fromSubtarget != null) {
				// Ensure the argument is raw, and set it as our subtarget.
				if (getRawType(parameter) == null) ok = false;

				if (subTarget == null) {
					subTarget = parameter.getType();

					// Also register a a new context info.
					String[] keys = fromSubtarget.value();
					if (keys.length == 0) {
						if (moduleNames == null) {
							keys = ORIGIN;
						} else {
							keys = new String[1 + moduleNames.length];
							keys[0] = ContextKeys.ORIGIN;
							System.arraycopy(moduleNames, 0, keys, 1, moduleNames.length);
						}
					}

					context.add(new ContextInfo(keys, parameter.getType()));
				} else {
					PlethoraCore.LOG.error("@PlethoraMethod method {}'s has multiple sub-targets.", name);
					ok = false;
				}
			} else if (fromContext != null) {
				if (getRawType(parameter) == null) ok = false;

				String[] keys = fromContext.value();
				if (keys.length == 0) keys = null;
				context.add(new ContextInfo(keys, parameter.getType()));
			} else {
				throw new IllegalStateException("Fallen through on annotation checks");
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
			if (modules == null) {
				LOG.error("@PlethoraMethod {} has no obvious target.", name);
				ok = false;
			} else {
				target = IModuleContainer.class;
			}
		}

		// Prefix the doc string with the type signature where possible.
		String docs = annotation.doc();
		if (Strings.isNullOrEmpty(docs)) {
			LOG.error("@PlethoraMethod {} does not have any documentation.", name);
			ok = false;
		} else if (!docs.startsWith("function(")) {
			if (!docs.startsWith("-- ")) {
				LOG.error("@PlethoraMethod {}'s documentation should start with 'function(' or '--'.", name);
				ok = false;
			}

			String signature = getSignature(method, name, parameters, contextIndex);
			if (signature == null) {
				LOG.error("@PlethoraMethod {} should specify a signature, due to dynamic arguments or return values.", name);
				ok = false;
			} else {
				docs = signature + " " + docs;
			}
		}

		// Extract some trivial properties
		String[] names = annotation.name();
		if (names.length == 0) names = new String[]{ method.getName() };

		// Get the marker interfaces.
		MarkerInterfaces markers = method.getAnnotation(MarkerInterfaces.class);
		Class<?>[] markerIfaces = markers == null || markers.value().length == 0 ? null : markers.value();

		if (!ok) return false;

		MethodInstance<?> instance = new MethodInstance<>(
			method, target, modId, names[0], docs,
			annotation.worldThread(), context.toArray(new ContextInfo[0]),
			contextIndex, modules, markerIfaces, subTarget
		);

		MethodRegistry.instance.registerMethod(instance);
		for (int i = 1; i < names.length; i++) {
			MethodRegistry.instance.registerMethod(new RegisteredMethod.Impl<>(
				instance.name(), instance.mod(), instance.target(),
				new RenamedMethod<>(names[i], (MethodInstance) instance)
			));
		}
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
				String modId = (String) asmData.getAnnotationInfo().get("modId");
				if (Strings.isNullOrEmpty(modId)) modId = null;
				if (modId != null && !Helpers.modLoaded(modId)) {
					PlethoraCore.LOG.debug("Skipping " + className + "#" + methodWhole + " as " + modId + " is not loaded or is blacklisted");
					continue;
				}

				Class<?> klass = Class.forName(className);
				Method method = findMethod(methodWhole, klass);

				if (method == null) {
					PlethoraCore.LOG.warn("Cannot find method" + className + "#" + methodWhole + ".");
					continue;
				}

				PlethoraCore.LOG.debug("Registering " + className + "#" + methodWhole);
				ok &= add(method, modId);
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

	@Nullable
	private static String getSignature(Method method, String name, Parameter[] parameters, int start) {
		if (start == parameters.length - 1 && parameters[parameters.length - 1].getType() == Object[].class) {
			return null;
		}

		Class<?> ret = method.getReturnType();
		if (ret == MethodResult.class || ret == Object[].class || ret == Object.class) return null;

		StringBuilder builder = new StringBuilder("function(");
		int optDepth = 0;
		for (int i = start; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			boolean optional = parameter.getAnnotation(Optional.class) != null;

			if (optional) {
				builder.append('[');
				optDepth++;
			} else {
				for (; optDepth > 0; optDepth--) builder.append(']');
			}

			if (i > start) builder.append(", ");
			builder.append(parameter.getName()).append(':').append(getTypeName(name, parameter.getType()));
		}

		for (; optDepth > 0; optDepth--) builder.append(']');
		builder.append(")");

		if (ret != void.class) {
			builder.append(":").append(getTypeName(name, ret));
			boolean optional = method.getAnnotation(Optional.class) != null;
			if (optional) builder.append("|nil");
		}

		return builder.toString();
	}

	@Nonnull
	private static String getTypeName(String name, Class<?> ty) {
		if (ty.isPrimitive()) {
			if (ty == long.class || ty == int.class || ty == short.class || ty == char.class || ty == byte.class) {
				return "int";
			}
			if (ty == double.class || ty == float.class) return "number";
			if (ty == boolean.class) return "boolean";

			LOG.warn("@PlethoraMethod {} has unknown primitive type {}", name, ty.getName());
			return ty.getName();
		} else if (Enum.class.isAssignableFrom(ty)) {
			return "string";
		} else if (Map.class.isAssignableFrom(ty) || ILuaObject.class.isAssignableFrom(ty)) {
			return "table";
		} else {
			ArgumentType<?> argTy = ArgumentTypeRegistry.get(ty);
			return argTy == null ? "value" : argTy.name();
		}
	}
}
