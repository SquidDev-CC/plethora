package org.squiddev.plethora.core.builder;

import com.google.common.base.Strings;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodBuilder;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

@Injects
public final class SubtargetedModuleObjectMethodBuilder extends MethodBuilder<SubtargetedModuleObjectMethod.Inject> {
	public SubtargetedModuleObjectMethodBuilder() throws NoSuchMethodException {
		super(SubtargetedModuleObjectMethod.class.getMethod("apply", Object.class, IContext.class, Object[].class), SubtargetedModuleObjectMethod.class);
	}

	@Override
	public Class<?> getTarget(@Nonnull Method method, @Nonnull SubtargetedModuleObjectMethod.Inject annotation) {
		return IModuleContainer.class;
	}

	@Override
	public void writeClass(@Nonnull Method method, @Nonnull SubtargetedModuleObjectMethod.Inject annotation, @Nonnull String className, @Nonnull ClassWriter writer) {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);

		String name = annotation.name();
		if (Strings.isNullOrEmpty(name)) name = method.getName();
		mv.visitLdcInsn(name);

		BuilderHelpers.writeModuleList(mv, annotation.module());

		mv.visitLdcInsn(Type.getType(annotation.target()));
		mv.visitInsn(annotation.worldThread() ? ICONST_1 : ICONST_0);

		mv.visitLdcInsn(annotation.priority());

		String doc = annotation.doc();
		if (Strings.isNullOrEmpty(doc)) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(doc);
		}

		mv.visitMethodInsn(INVOKESPECIAL, "org/squiddev/plethora/api/module/SubtargetedModuleObjectMethod", "<init>", "(Ljava/lang/String;Ljava/util/Set;Ljava/lang/Class;ZILjava/lang/String;)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(6, 1);
		mv.visitEnd();
	}

	@Nonnull
	@Override
	protected Class<?>[] getArgumentTypes(@Nonnull Method method, @Nonnull SubtargetedModuleObjectMethod.Inject annotation) {
		return new Class<?>[]{annotation.target(), IContext.class, Object[].class};
	}
}
