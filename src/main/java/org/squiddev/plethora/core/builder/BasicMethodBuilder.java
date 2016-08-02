package org.squiddev.plethora.core.builder;

import com.google.common.base.Strings;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

@IMethodBuilder.Inject(BasicMethod.Inject.class)
public class BasicMethodBuilder extends MethodBuilder<BasicMethod.Inject> {
	public BasicMethodBuilder() throws NoSuchMethodException {
		super(IMethod.class.getMethod("apply", IUnbakedContext.class, Object[].class), BasicMethod.class);
	}

	@Override
	public Class<?> getTarget(@Nonnull Method method, @Nonnull BasicMethod.Inject annotation) {
		return annotation.value();
	}

	@Override
	public void writeClass(@Nonnull Method method, @Nonnull BasicMethod.Inject annotation, @Nonnull String className, @Nonnull ClassWriter writer) {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);

		String name = annotation.name();
		if (Strings.isNullOrEmpty(name)) name = method.getName();
		mv.visitLdcInsn(name);

		mv.visitLdcInsn(annotation.priority());

		String doc = annotation.doc();
		if (Strings.isNullOrEmpty(doc)) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(doc);
		}

		mv.visitMethodInsn(INVOKESPECIAL, "org/squiddev/plethora/api/method/BasicMethod", "<init>", "(Ljava/lang/String;ILjava/lang/String;)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(4, 1);
		mv.visitEnd();
	}
}
