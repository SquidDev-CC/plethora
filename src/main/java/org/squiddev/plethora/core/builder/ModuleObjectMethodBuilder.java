package org.squiddev.plethora.core.builder;

import com.google.common.base.Strings;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethodBuilder;
import org.squiddev.plethora.api.method.MethodBuilder;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleObjectMethod;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

@IMethodBuilder.Inject(ModuleObjectMethod.Inject.class)
public class ModuleObjectMethodBuilder extends MethodBuilder<ModuleObjectMethod.Inject> {
	public ModuleObjectMethodBuilder() throws NoSuchMethodException {
		super(ModuleObjectMethod.class.getMethod("apply", IContext.class, Object[].class), ModuleObjectMethod.class);
	}

	@Override
	public Class<?> getTarget(@Nonnull Method method, @Nonnull ModuleObjectMethod.Inject annotation) {
		return IModuleContainer.class;
	}

	@Override
	public void writeClass(@Nonnull Method method, @Nonnull ModuleObjectMethod.Inject annotation, @Nonnull String className, @Nonnull ClassWriter writer) {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);

		String name = annotation.name();
		if (Strings.isNullOrEmpty(name)) name = method.getName();
		mv.visitLdcInsn(name);

		BuilderHelpers.writeModuleList(mv, annotation.module());

		mv.visitInsn(annotation.worldThread() ? ICONST_1 : ICONST_0);

		mv.visitLdcInsn(annotation.priority());

		String doc = annotation.doc();
		if (Strings.isNullOrEmpty(doc)) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(doc);
		}

		mv.visitMethodInsn(INVOKESPECIAL, "org/squiddev/plethora/api/module/ModuleObjectMethod", "<init>", "(Ljava/lang/String;Ljava/util/Set;ZILjava/lang/String;)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(6, 1);
		mv.visitEnd();
	}
}
