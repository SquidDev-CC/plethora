package org.squiddev.plethora.core.builder;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public final class BuilderHelpers {
	private BuilderHelpers() {
	}

	public static void writeModuleList(MethodVisitor mv, String[] modules) {
		mv.visitLdcInsn(modules.length);
		mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/Sets", "newHashSetWithExpectedSize", "(I)Ljava/util/HashSet;", false);
		for (String module : modules) {
			// Duplicate hash set
			mv.visitInsn(DUP);

			// Construct resource location
			mv.visitTypeInsn(NEW, "net/minecraft/util/ResourceLocation");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(module);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/util/ResourceLocation", "<init>", "(Ljava/lang/String;)V", false);

			// Insert into HashSet
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)Z", false);
			mv.visitInsn(POP);
		}
	}
}
