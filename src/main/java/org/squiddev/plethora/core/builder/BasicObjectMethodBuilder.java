package org.squiddev.plethora.core.builder;

import com.google.common.base.Strings;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethodBuilder;
import org.squiddev.plethora.api.method.MethodBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

@IMethodBuilder.Inject(BasicObjectMethod.Inject.class)
public class BasicObjectMethodBuilder extends MethodBuilder<BasicObjectMethod.Inject> {
	public BasicObjectMethodBuilder() throws NoSuchMethodException {
		super(BasicObjectMethod.class.getMethod("apply", IContext.class, Object[].class), BasicObjectMethod.class);
	}

	@Override
	public Class<?> getTarget(@Nonnull Method method, @Nonnull BasicObjectMethod.Inject annotation) {
		return annotation.value();
	}

	@Override
	public void writeClass(@Nonnull Method method, @Nonnull BasicObjectMethod.Inject annotation, @Nonnull String className, @Nonnull ClassWriter writer) {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);

		String name = annotation.name();
		if (Strings.isNullOrEmpty(name)) name = method.getName();
		mv.visitLdcInsn(name);

		mv.visitInsn(annotation.worldThread() ? ICONST_1 : ICONST_0);
		mv.visitLdcInsn(annotation.priority());

		String doc = annotation.doc();
		if (Strings.isNullOrEmpty(doc)) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(doc);
		}

		mv.visitMethodInsn(INVOKESPECIAL, "org/squiddev/plethora/api/method/BasicObjectMethod", "<init>", "(Ljava/lang/String;ZILjava/lang/String;)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(5, 1);
		mv.visitEnd();
	}
}
