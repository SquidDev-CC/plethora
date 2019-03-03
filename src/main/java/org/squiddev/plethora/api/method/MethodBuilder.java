package org.squiddev.plethora.api.method;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * A base class for {@link IMethodBuilder}s which provides automatic validation
 */
public abstract class MethodBuilder<T extends Annotation> implements IMethodBuilder<T> {
	private final Method method;
	private final String methodSignature;
	private final String superName;

	private final String[] interfaces;
	private final String[] exceptions;

	/**
	 * Construct a new method builder which overrides a superclass and delegates to another method.
	 *
	 * @param method The method to delegate to
	 */
	public MethodBuilder(Method method, Class<? extends IMethod> superClass) {
		this.method = method;
		this.methodSignature = Type.getMethodDescriptor(method);

		superName = Type.getInternalName(superClass);

		Class<?>[] interfaceClasses = superClass.getInterfaces();
		String[] interfaceNames = interfaces = new String[interfaceClasses.length];
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaceNames[i] = Type.getInternalName(interfaceClasses[i]);
		}

		Class<?>[] exceptionClasses = method.getExceptionTypes();
		String[] exceptionNames = exceptions = new String[exceptionClasses.length];
		for (int i = 0; i < exceptionNames.length; i++) {
			exceptionNames[i] = Type.getInternalName(exceptionClasses[i]);
		}
	}

	@Nonnull
	@Override
	public final byte[] writeClass(@Nonnull Method method, @Nonnull T annotation, @Nonnull Set<Class<?>> markerInterfaces, @Nonnull String name) {
		Set<String> allInterfaces = Sets.newHashSetWithExpectedSize(markerInterfaces.size() + interfaces.length);
		Collections.addAll(allInterfaces, interfaces);
		for (Class<?> klass : markerInterfaces) {
			allInterfaces.add(Type.getInternalName(klass));
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		writer.visit(V1_6, ACC_PUBLIC | ACC_FINAL, name, null, superName, allInterfaces.toArray(new String[0]));

		MethodVisitor invoke = writer.visitMethod(ACC_PUBLIC, getMethod().getName(), methodSignature, null, exceptions);
		invoke.visitCode();
		writeApply(method, annotation, name, invoke);
		invoke.visitEnd();

		writeClass(method, annotation, name, writer);

		writer.visitEnd();
		return writer.toByteArray();
	}

	/**
	 * Write the constructor and any other required methods.
	 *
	 * @param method     The method being generated
	 * @param annotation The annotation data for this method
	 * @param className  The internal name of the class to generate
	 * @param writer     The writer to write to.
	 */
	public abstract void writeClass(@Nonnull Method method, @Nonnull T annotation, @Nonnull String className, @Nonnull ClassWriter writer);

	/**
	 * Used to write the body of the apply method
	 *
	 * @param method     The method we are generating for
	 * @param annotation The annotation data of the method we are generating for
	 * @param className  The class name we are generating
	 * @param mv         The visitor to write to. This has had {@link MethodVisitor#visitCode()} already called.
	 *                   You need not call {@link MethodVisitor#visitEnd()}.
	 */
	protected void writeApply(@Nonnull Method method, @Nonnull T annotation, @Nonnull String className, @Nonnull MethodVisitor mv) {
		Class<?>[] parameterTypes = getMethod().getParameterTypes();
		Class<?>[] childParamTypes = method.getParameterTypes();

		if (parameterTypes.length != childParamTypes.length) {
			throw new IllegalStateException(String.format("Expected %d arguments, got %d (expected signature of (%s))",
				parameterTypes.length, childParamTypes.length, Joiner.on(", ").join(Arrays.stream(parameterTypes).map(Class::getTypeName).iterator())));
		}

		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> arg = parameterTypes[i];
			if (arg.isPrimitive()) {
				if (arg != childParamTypes[i]) {
					throw new IllegalStateException("Expected argument " + arg.getName() + ", got " + childParamTypes[i].getName());
				}

				if (arg == int.class || arg == boolean.class || arg == byte.class || arg == short.class || arg == char.class) {
					mv.visitVarInsn(ILOAD, i + 1);
				} else if (arg == float.class) {
					mv.visitVarInsn(FLOAD, i + 1);
				} else if (arg == double.class) {
					mv.visitVarInsn(DLOAD, i + 1);
				} else if (arg == long.class) {
					mv.visitVarInsn(LLOAD, i + 1);
				} else {
					throw new IllegalStateException("Unknown primitive " + arg);
				}
			} else {
				mv.visitVarInsn(ALOAD, i + 1);

				if (arg != childParamTypes[i]) {
					mv.visitTypeInsn(CHECKCAST, Type.getInternalName(childParamTypes[i]));
				}
			}
		}

		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method), false);

		Class<?> ret = getMethod().getReturnType();
		Class<?> childRet = method.getReturnType();
		if (ret.isPrimitive()) {
			if (ret != childRet) {
				throw new IllegalStateException("Expected argument " + ret.getName() + ", got " + childRet.getName());
			}

			if (ret == int.class || ret == boolean.class || ret == byte.class || ret == short.class || ret == char.class) {
				mv.visitInsn(IRETURN);
			} else if (ret == float.class) {
				mv.visitInsn(FRETURN);
			} else if (ret == double.class) {
				mv.visitInsn(DRETURN);
			} else if (ret == long.class) {
				mv.visitInsn(LRETURN);
			} else if (ret == void.class) {
				mv.visitInsn(RETURN);
			} else {
				throw new IllegalStateException("Unknown primitive " + ret);
			}
		} else {
			mv.visitInsn(ARETURN);

			if (ret != childRet) {
				mv.visitTypeInsn(CHECKCAST, Type.getInternalName(ret));
			}
		}

		mv.visitMaxs(parameterTypes.length, parameterTypes.length + 1);
	}

	/**
	 * Get the method this builder delegates to
	 *
	 * @return The builder this method delegates to
	 */
	public final Method getMethod() {
		return method;
	}

	/**
	 * Get the return type for this method
	 *
	 * @return The method's return type
	 */
	@Nonnull
	protected Class<?> getReturnType(@Nonnull Method method, @Nonnull T annotation) {
		return method.getReturnType();
	}

	@Nonnull
	protected Class<?>[] getArgumentTypes(@Nonnull Method method, @Nonnull T annotation) {
		return method.getParameterTypes();
	}

	@Nonnull
	@Override
	public List<String> validate(@Nonnull Method method, @Nonnull T annotation) {
		List<String> errors = Lists.newArrayList();

		Class<?> returnType = getReturnType(method, annotation);
		if (method.getReturnType() != returnType) {
			errors.add("Bad return type: expected " + returnType + ", got " + method.getReturnType().getName());
		}

		Class<?>[] args = method.getParameterTypes();
		Class<?>[] expectedArgs = getArgumentTypes(method, annotation);
		if (args.length != expectedArgs.length) {
			errors.add("Bad arg count: expected " + expectedArgs.length + ", got " + args.length);
		} else {
			for (int i = 0; i < args.length; i++) {
				Class<?> arg = args[i];
				Class<?> expectedArg = expectedArgs[i];

				if (arg != expectedArg) {
					errors.add("Bad arg #" + (i + 1) + ": expected " + expectedArg.getName() + ", got " + arg.getName());
				}
			}
		}

		return errors;
	}
}
