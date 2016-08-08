package org.squiddev.plethora.core.builder;

import com.google.common.base.Strings;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.method.IMethodBuilder;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodBuilder;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

@IMethodBuilder.Inject(TargetedModuleMethod.Inject.class)
public class TargetedModuleMethodBuilder extends MethodBuilder<TargetedModuleMethod.Inject> {
	public TargetedModuleMethodBuilder() throws NoSuchMethodException {
		super(TargetedModuleMethod.class.getMethod("apply", IUnbakedContext.class, Object[].class), TargetedModuleMethod.class);
	}

	@Override
	public Class<?> getTarget(@Nonnull Method method, @Nonnull TargetedModuleMethod.Inject annotation) {
		return IModule.class;
	}

	@Override
	public void writeClass(@Nonnull Method method, @Nonnull TargetedModuleMethod.Inject annotation, @Nonnull String className, @Nonnull ClassWriter writer) {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);

		String name = annotation.name();
		if (Strings.isNullOrEmpty(name)) name = method.getName();
		mv.visitLdcInsn(name);

		String module = annotation.module();
		mv.visitTypeInsn(NEW, "net/minecraft/util/ResourceLocation");
		mv.visitInsn(DUP);
		mv.visitLdcInsn(module);
		mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/util/ResourceLocation", "<init>", "(Ljava/lang/String;)V", false);

		mv.visitLdcInsn(Type.getType(annotation.target()));

		mv.visitLdcInsn(annotation.priority());

		String doc = annotation.doc();
		if (Strings.isNullOrEmpty(doc)) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitLdcInsn(doc);
		}

		mv.visitMethodInsn(INVOKESPECIAL, "org/squiddev/plethora/api/module/TargetedModuleMethod", "<init>", "(Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;ILjava/lang/String;)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(6, 1);
		mv.visitEnd();
	}
}
