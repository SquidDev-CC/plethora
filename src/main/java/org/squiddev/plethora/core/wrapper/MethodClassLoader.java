package org.squiddev.plethora.core.wrapper;

import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;
import dan200.computercraft.api.lua.ArgumentHelper;
import net.minecraft.util.ResourceLocation;
import org.objectweb.asm.*;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.method.wrapper.*;

import javax.annotation.Nullable;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;
import static org.squiddev.plethora.core.PlethoraCore.LOG;
import static org.squiddev.plethora.core.wrapper.ClassWriterHelpers.*;

final class MethodClassLoader extends ClassLoader {
	public static final MethodClassLoader INSTANCE = new MethodClassLoader();

	private static final String INTERNAL_OBJECT = Type.getInternalName(Object.class);
	private static final String INTERNAL_DELEGATE = Type.getInternalName(IMethod.Delegate.class);
	private static final String INTERNAL_ARGUMENT_HELPER = Type.getInternalName(ArgumentHelper.class);
	private static final String INTERNAL_ARGUMENT_HELPER_II = Type.getInternalName(org.squiddev.plethora.api.method.ArgumentHelper.class);
	private static final String INTERNAL_ARGUMENT_TYPE = Type.getInternalName(ArgumentType.class);
	private static final String INTERNAL_METHOD_RESULT = Type.getInternalName(MethodResult.class);
	private static final String INTERNAL_UNBAKED_CONTEXT = Type.getInternalName(IUnbakedContext.class);
	private static final String INTERNAL_BAKED_CONTEXT = Type.getInternalName(IContext.class);

	private static final Type ID_CALL_SIG = Type.getType("()Ljava/lang/Object;");
	private static final Type ID_CALL_SPECIAL_SIG = Type.getType("()Lorg/squiddev/plethora/api/method/MethodResult;");
	private static final Handle ID_HANDLE = new Handle(
		Opcodes.H_INVOKESTATIC,
		"java/lang/invoke/LambdaMetafactory", "metafactory",
		"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
		false
	);

	private static final int IDX_CTX = 1;
	private static final int IDX_ARG = 2;

	private static final AtomicInteger METHOD_ID = new AtomicInteger();

	private MethodClassLoader() {
		super(MethodClassLoader.class.getClassLoader());
	}

	@SuppressWarnings("unchecked")
	<T> MethodInstance.Delegate<T> build(MethodInstance method) {
		try {
			Class<?> klass = writeClass(method);
			if (klass == null) throw BadWrapperException.INSTANCE;

			return klass.asSubclass(MethodInstance.Delegate.class).newInstance();
		} catch (BadWrapperException e) {
			throw e;
		} catch (ClassFormatError | ReflectiveOperationException | RuntimeException e) {
			LOG.error(
				"Error generating wrapper for {}.{}",
				method.method.getDeclaringClass().getName(), method.method.getName(), e
			);

			throw BadWrapperException.INSTANCE;
		}
	}

	@Nullable
	private Class<?> writeClass(MethodInstance<?> methodInstance) {
		Method method = methodInstance.method;
		Parameter[] parameters = method.getParameters();

		String className = method.getDeclaringClass().getName() + "$" + method.getName() + METHOD_ID.getAndIncrement();
		String internalName = className.replace(".", "/");

		// Construct a public final class which extends Object and implements MethodInstance.Delegate
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, internalName, null, INTERNAL_OBJECT, new String[]{ INTERNAL_DELEGATE });
		cw.visitSource("Plethora generated method", null);

		StringBuilder runDescBuilder = new StringBuilder();
		runDescBuilder.append("(L");
		runDescBuilder.append(INTERNAL_UNBAKED_CONTEXT);
		runDescBuilder.append(";");
		for (int i = methodInstance.totalContext; i < parameters.length; i++) {
			runDescBuilder.append(Type.getDescriptor(parameters[i].getType()));
		}
		String runDescPre = runDescBuilder.append(")L").toString();

		{ // Constructor just invokes super.
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mw.visitCode();
			mw.visitVarInsn(ALOAD, 0);
			mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mw.visitInsn(RETURN);
			mw.visitMaxs(0, 0);
			mw.visitEnd();
		}

		String runDesc = runDescPre + INTERNAL_METHOD_RESULT + ";";
		{ // The main apply method validates arguments and delegates to the static invoker.
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "apply", "(Lorg/squiddev/plethora/api/method/IUnbakedContext;[Ljava/lang/Object;)Lorg/squiddev/plethora/api/method/MethodResult;", null, null);
			mw.visitCode();
			mw.visitVarInsn(ALOAD, IDX_CTX); // Load the unbaked context

			// Load arguments
			if (methodInstance.totalContext == parameters.length - 1 && parameters[parameters.length - 1].getType() == Object[].class) {
				// If our signature is of the form `Object[]`, then the method will do its own argument parsing:
				// just pass it on.
				mw.visitVarInsn(ALOAD, IDX_ARG);
			} else {
				// Otherwise use our argument converters.
				boolean argsOk = true;
				int luaIndex = 0;
				for (int i = methodInstance.totalContext; i < parameters.length; i++, luaIndex++) {
					Parameter parameter = parameters[i];
					if (!loadLuaArg(mw, luaIndex, parameter)) argsOk = false;
				}
				if (!argsOk) return null;
			}

			// And dispatch
			if (methodInstance.worldThread) {
				String runDescDynamic = runDescPre + "java/util/concurrent/Callable;";
				mw.visitInvokeDynamicInsn(
					"call", runDescDynamic, ID_HANDLE,
					ID_CALL_SIG, new Handle(Opcodes.H_INVOKESTATIC, internalName, "run", runDesc, false), ID_CALL_SPECIAL_SIG
				);
				mw.visitMethodInsn(INVOKESTATIC, INTERNAL_METHOD_RESULT, "nextTick", "(Ljava/util/concurrent/Callable;)L" + INTERNAL_METHOD_RESULT + ";", false);
			} else {
				mw.visitMethodInsn(INVOKESTATIC, internalName, "run", runDesc, false);
			}

			mw.visitInsn(ARETURN);

			mw.visitMaxs(0, 0);
			mw.visitEnd();
		}

		{ // The static invoke method loads any context info and delegates to the original.
			MethodVisitor mw = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "run", runDesc, null, null);
			mw.visitCode();

			mw.visitVarInsn(ALOAD, 0);
			mw.visitMethodInsn(INVOKEINTERFACE, INTERNAL_UNBAKED_CONTEXT, methodInstance.worldThread ? "bake" : "safeBake", "()L" + INTERNAL_BAKED_CONTEXT + ";", true);
			mw.visitVarInsn(ASTORE, 0);

			for (int i = 0; i < methodInstance.totalContext; i++) loadContextArg(mw, methodInstance, parameters[i]);

			int varIndex = 1;
			for (int i = methodInstance.totalContext; i < parameters.length; i++) {
				varIndex += loadVar(mw, parameters[i].getType(), varIndex);
			}

			// Now invoke the underlying method.
			mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method), false);

			// Convert to a method result
			Class<?> ret = method.getReturnType();
			if (ret != MethodResult.class) {
				if (ret == void.class) {
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_METHOD_RESULT, "empty", "()L" + INTERNAL_METHOD_RESULT + ";", false);
				} else if (ret.isPrimitive()) {
					Class<?> boxed = Primitives.wrap(ret);
					mw.visitMethodInsn(INVOKESTATIC, Type.getInternalName(boxed), "valueOf", "(" + Type.getDescriptor(ret) + ")" + Type.getDescriptor(boxed), false);
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_METHOD_RESULT, "result", "(Ljava/lang/Object;)L" + INTERNAL_METHOD_RESULT + ";", false);
				} else if (ret == Object[].class) {
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_METHOD_RESULT, "result", "([Ljava/lang/Object;)L" + INTERNAL_METHOD_RESULT + ";", false);
				} else {
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_METHOD_RESULT, "result", "(Ljava/lang/Object;)L" + INTERNAL_METHOD_RESULT + ";", false);
				}
			}

			mw.visitInsn(ARETURN);

			mw.visitMaxs(0, 0);
			mw.visitEnd();
		}

		cw.visitEnd();

		byte[] result = cw.toByteArray();
		validateClass(result, this);
		return defineClass(className, result, 0, result.length, method.getClass().getProtectionDomain());
	}

	private static boolean loadLuaArg(MethodVisitor mw, int index, Parameter parameter) {
		Class<?> argument = parameter.getType();

		if (argument.isPrimitive()) {
			mw.visitVarInsn(ALOAD, IDX_ARG);
			loadInt(mw, index);
			Optional def = parameter.getAnnotation(Optional.class);
			if (argument == int.class || argument == short.class || argument == char.class || argument == byte.class) {
				if (def == null) {
					visitGet(mw, "Int", "I");
				} else {
					loadInt(mw, def.defInt());
					visitOpt(mw, "Int", "I");
				}
			} else if (argument == boolean.class) {
				if (def == null) {
					visitGet(mw, "Boolean", "Z");
				} else {
					loadInt(mw, def.defBool() ? 1 : 0);
					visitOpt(mw, "Boolean", "Z");
				}
			} else if (argument == long.class) {
				if (def == null) {
					visitGet(mw, "Long", "J");
				} else {
					loadLong(mw, def.defLong());
					visitOpt(mw, "Long", "J");
				}
			} else if (argument == double.class) {
				if (def == null) {
					visitGet(mw, "Double", "D");
				} else {
					loadDouble(mw, def.defDoub());
					visitOpt(mw, "Double", "D");
				}
			} else if (argument == float.class) {
				if (def == null) {
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER_II, "getFloat", "([Ljava/lang/Object;I)F", false);
				} else {
					loadFloat(mw, (float) def.defDoub());
					mw.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER_II, "optFloat", "([Ljava/lang/Object;IF)F", false);
				}
			} else {
				Executable method = parameter.getDeclaringExecutable();
				LOG.error(
					"Argument {} for @PlethoraMethod {}.{} has an unknown primitive type {}.",
					parameter.getName(), method.getDeclaringClass().getName(), method.getName(), argument
				);
				return false;
			}
		} else if (Enum.class.isAssignableFrom(argument) && argument != Enum.class) {
			// We have a special handler for enums
			mw.visitVarInsn(ALOAD, IDX_ARG);
			loadInt(mw, index);
			mw.visitLdcInsn(Type.getType(argument));
			if (parameter.getAnnotation(Optional.class) == null) {
				mw.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER_II, "getEnum", "([Ljava/lang/Object;ILjava/lang/Class;)Ljava/lang/Enum;", false);
			} else {
				mw.visitInsn(ACONST_NULL);
				mw.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER_II, "optEnum", "([Ljava/lang/Object;ILjava/lang/Class;Ljava/lang/Enum;)Ljava/lang/Enum;", false);
			}
			mw.visitTypeInsn(CHECKCAST, Type.getInternalName(argument));
		} else {
			Field field = ArgumentTypeRegistry.getField(argument);
			if (field == null) {
				Executable method = parameter.getDeclaringExecutable();
				LOG.error(
					"Argument {} for @PlethoraMethod {}.{} has no obvious converter for {}.",
					parameter.getName(), method.getDeclaringClass().getName(), method.getName(), argument
				);
				return false;
			}

			mw.visitFieldInsn(GETSTATIC, Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(field.getType()));
			mw.visitVarInsn(ALOAD, IDX_ARG);
			loadInt(mw, index);
			mw.visitMethodInsn(
				INVOKEINTERFACE, INTERNAL_ARGUMENT_TYPE,
				parameter.getAnnotation(Optional.class) == null ? "get" : "opt",
				"([Ljava/lang/Object;I)Ljava/lang/Object;", true
			);
			mw.visitTypeInsn(CHECKCAST, Type.getInternalName(argument));
		}

		return true;
	}

	private static void loadContextArg(MethodVisitor mw, MethodInstance method, Parameter parameter) {
		// If we're an IContext, just load that directly.
		if (parameter.getType() == IContext.class || parameter.getType() == IPartialContext.class) {
			mw.visitVarInsn(ALOAD, 0);
			return;
		}

		FromTarget target = parameter.getAnnotation(FromTarget.class);
		if (target != null) {
			mw.visitVarInsn(ALOAD, 0);
			mw.visitMethodInsn(INVOKEINTERFACE, INTERNAL_BAKED_CONTEXT, "getTarget", "()Ljava/lang/Object;", true);
			mw.visitTypeInsn(CHECKCAST, Type.getInternalName(parameter.getType()));
			return;
		}

		FromSubtarget subTarget = parameter.getAnnotation(FromSubtarget.class);
		if (subTarget != null) {
			// Use getContext(String, Class) until we have a success
			String[] names = subTarget.value();
			if (names.length == 0) {
				Label success = new Label();
				visitGetContext(mw, ContextKeys.ORIGIN, parameter.getType());
				for (ResourceLocation name : method.modules) {
					mw.visitInsn(DUP);
					mw.visitJumpInsn(IFNONNULL, success);
					mw.visitInsn(POP);
					visitGetContext(mw, name.toString(), parameter.getType());
				}
				mw.visitLabel(success);
			} else {
				visitGetContext(mw, names, parameter.getType());
			}
			return;
		}

		FromContext context = parameter.getAnnotation(FromContext.class);
		if (context != null) {
			String[] names = context.value();
			if (names.length == 0 || (names.length == 1 && Strings.isNullOrEmpty(names[0]))) {
				// use getContext(Class)
				mw.visitVarInsn(ALOAD, 0);
				mw.visitLdcInsn(Type.getType(parameter.getType()));
				mw.visitMethodInsn(INVOKEINTERFACE, INTERNAL_BAKED_CONTEXT, "getContext", "(Ljava/lang/Class;)Ljava/lang/Object;", true);
				mw.visitTypeInsn(CHECKCAST, Type.getInternalName(parameter.getType()));
			} else {
				// Use getContext(String, Class) until we have a success
				visitGetContext(mw, names, parameter.getType());
			}
			return;
		}

		throw new IllegalStateException("Fallthrough in annotation checks.");
	}

	private static void visitGet(MethodVisitor visitor, String name, String type) {
		visitor.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER, "get" + name, "([Ljava/lang/Object;I)" + type, false);
	}

	private static void visitOpt(MethodVisitor visitor, String name, String type) {
		visitor.visitMethodInsn(INVOKESTATIC, INTERNAL_ARGUMENT_HELPER, "opt" + name, "([Ljava/lang/Object;I" + type + ")" + type, false);
	}

	private static void visitGetContext(MethodVisitor mw, String key, Class<?> type) {
		mw.visitVarInsn(ALOAD, 0);
		mw.visitLdcInsn(key);
		mw.visitLdcInsn(Type.getType(type));
		mw.visitMethodInsn(INVOKEINTERFACE, INTERNAL_BAKED_CONTEXT, "getContext", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", true);
		mw.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
	}

	private static void visitGetContext(MethodVisitor mw, String[] keys, Class<?> type) {
		// Use getContext(String, Class) until we have a success
		Label success = new Label();

		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				mw.visitInsn(DUP);
				mw.visitJumpInsn(IFNONNULL, success);
				mw.visitInsn(POP);
			}

			visitGetContext(mw, keys[i], type);
		}
	}
}
