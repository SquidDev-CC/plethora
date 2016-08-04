package org.squiddev.plethora.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IMethodBuilder;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds classes based off of types
 */
public final class MethodTypeBuilder extends ClassLoader {
	private static final String DOTTED_PREFIX = "org.squiddev.plethora.generated.";
	private static final String INTERNAL_PREFIX = "org/squiddev/plethora/generated/";

	public static final MethodTypeBuilder instance = new MethodTypeBuilder();

	private final Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();

	private final Map<Class<? extends Annotation>, IMethodBuilder> annotations = Maps.newHashMap();

	private MethodTypeBuilder() {
		super(MethodTypeBuilder.class.getClassLoader());
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> Class<? extends IMethod> loadMethod(Method method, T meta, IMethodBuilder<T> builder) {
		// Validation
		{
			int modifiers = method.getModifiers();
			List<String> issues = Lists.newArrayList();

			if (!Modifier.isPublic(modifiers)) {
				String modifier = "<unknown>";
				if ((modifiers & Modifier.PUBLIC) != 0) {
					modifier = "public";
				} else if ((modifiers & Modifier.PROTECTED) != 0) {
					modifier = "protected";
				} else if ((modifiers & Modifier.PRIVATE) != 0) {
					modifier = "private";
				}

				issues.add("Method is not public: is " + modifier);
			}

			if (!Modifier.isStatic(modifiers)) {
				issues.add("Method is not static");
			}

			issues.addAll(builder.validate(method, meta));

			if (issues.size() > 0) {
				throw new IllegalStateException("Issues encountered loading " + method + "\n" + Joiner.on("\n").join(issues));
			}
		}

		String classPart = (method.getDeclaringClass().getName() + "$" + method.getName()).replace('.', '_');
		String dottedName = DOTTED_PREFIX + classPart;
		String internalName = INTERNAL_PREFIX + classPart;

		Class<?> klass = add(dottedName, builder.writeClass(method, meta, internalName));
		if (!IMethod.class.isAssignableFrom(klass)) {
			throw new IllegalStateException("Issues encountered loading " + method + " from " + builder + ": " + klass + " is not assignable to IMethod");
		}

		return (Class<? extends IMethod>) klass;
	}

	private Class<?> add(String name, byte[] bytes) {
		if (ConfigCore.Testing.bytecodeVerify) {
			validateClass(new ClassReader(bytes), getClass().getClassLoader());
		}

		Class<?> klass = defineClass(name, bytes, 0, bytes.length);
		loaded.put(name, klass);
		return klass;
	}

	private void validateClass(ClassReader reader, ClassLoader loader) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);

		Exception error = null;
		try {
			CheckClassAdapter.verify(reader, loader, false, printWriter);
		} catch (Exception e) {
			error = e;
		}

		String contents = writer.toString();
		if (error != null || contents.length() > 0) {
			reader.accept(new TraceClassVisitor(printWriter), 0);
			throw new IllegalStateException(writer.toString(), error);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		DebugLogger.debug("Cached: " + name + " → " + findLoadedClass(name));
		Class<?> klass = loaded.get(name);
		if (klass != null) {
			DebugLogger.debug("Loaded: " + name + " → " + klass);
			return klass;
		}

		return super.findClass(name);
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> void loadAsm(ASMDataTable asmDataTable, Class<T> annotation, IMethodBuilder<T> builder) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(annotation.getName())) {
			String className = asmData.getClassName();
			String methodWhole = asmData.getObjectName();

			try {
				if (Helpers.classBlacklisted(ConfigCore.Blacklist.blacklistMethods, className)) {
					DebugLogger.debug("Ignoring " + className + "#" + methodWhole);
					continue;
				}

				if (Helpers.classBlacklisted(ConfigCore.Blacklist.blacklistMethods, className + "#" + methodWhole)) {
					DebugLogger.debug("Ignoring " + className + "#" + methodWhole);
					continue;
				}

				Class<?> klass = Class.forName(className);
				Method method = findMethod(methodWhole, klass);

				if (method == null) {
					DebugLogger.warn("Cannot find method" + className + "#" + methodWhole + ". This has probably been removed through @Optional.");
					continue;
				}

				DebugLogger.debug("Registering " + className + "#" + methodWhole);

				T meta = method.getAnnotation(annotation);
				Class<? extends IMethod> builtClass = loadMethod(method, meta, builder);
				MethodRegistry.instance.registerMethod(builder.getTarget(method, meta), builtClass.newInstance());
			} catch (Throwable e) {
				if (ConfigCore.Testing.strict) {
					throw new IllegalStateException("Failed to load: " + className + "#" + methodWhole, e);
				} else {
					DebugLogger.error("Failed to load: " + className + "#" + methodWhole, e);
				}
			}
		}
	}

	public <T extends Annotation> void addBuilder(Class<T> klass, IMethodBuilder<T> builder) {
		IMethodBuilder<?> other = annotations.get(klass);
		if (other != null) {
			throw new IllegalArgumentException("Duplicate builder for " + klass.getName() + ": trying to replace " + other + " with " + builder);
		}

		annotations.put(klass, builder);
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IMethodBuilder.Inject.class.getName())) {
			String name = asmData.getClassName();
			try {
				DebugLogger.debug("Registering " + name);

				Class<?> asmClass = Class.forName(name);
				IMethodBuilder instance = asmClass.asSubclass(IMethodBuilder.class).newInstance();

				Map<String, Object> info = asmData.getAnnotationInfo();
				Class<? extends Annotation> target = Class.forName(((Type) info.get("value")).getClassName()).asSubclass(Annotation.class);
				addBuilder(target, instance);
			} catch (Throwable e) {
				DebugLogger.error("Failed to load: " + name, e);
			}
		}

		for (Map.Entry<Class<? extends Annotation>, IMethodBuilder> builder : annotations.entrySet()) {
			loadAsm(asmDataTable, builder.getKey(), builder.getValue());
		}
	}

	private Method findMethod(String methodWhole, Class<?> klass) {
		int offset = methodWhole.indexOf('(');
		String methodName = methodWhole.substring(0, offset);
		String methodDesc = methodWhole.substring(offset);

		for (Method method : klass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				if (Type.getMethodDescriptor(method).equals(methodDesc)) {
					return method;
				}
			}
		}

		return null;
	}
}
