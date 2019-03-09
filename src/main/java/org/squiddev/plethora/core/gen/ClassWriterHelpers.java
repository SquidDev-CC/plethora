package org.squiddev.plethora.core.gen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.plethora.core.ConfigCore;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.objectweb.asm.Opcodes.*;

class ClassWriterHelpers {
	static void loadInt(MethodVisitor visitor, int value) {
		if (value >= -1 && value <= 5) {
			visitor.visitInsn(ICONST_0 + value);
		} else {
			visitor.visitLdcInsn(value);
		}
	}

	static void loadLong(MethodVisitor visitor, long value) {
		if (value >= 0 && value <= 1) {
			visitor.visitInsn(LCONST_0 + (int) value);
		} else {
			visitor.visitLdcInsn(value);
		}
	}

	static void loadDouble(MethodVisitor visitor, double value) {
		if (value == 0 || value == 1) {
			visitor.visitInsn(DCONST_0 + (int) value);
		} else {
			visitor.visitLdcInsn(value);
		}
	}

	static void loadFloat(MethodVisitor visitor, float value) {
		if (value == 0 || value == 1 || value == 2) {
			visitor.visitInsn(FCONST_0 + (int) value);
		} else {
			visitor.visitLdcInsn(value);
		}
	}

	static int loadVar(MethodVisitor mw, Class<?> klass, int slot) {
		if (klass == double.class) {
			mw.visitVarInsn(DLOAD, slot);
			return 2;
		} else if (klass == long.class) {
			mw.visitVarInsn(LLOAD, slot);
			return 2;
		} else if (klass == float.class) {
			mw.visitVarInsn(FLOAD, slot);
			return 1;
		} else if (klass == int.class || klass == short.class || klass == byte.class || klass == boolean.class) {
			mw.visitVarInsn(ILOAD, slot);
			return 1;
		} else {
			mw.visitVarInsn(ALOAD, slot);
			return 1;
		}
	}

	static void validateClass(byte[] bytes, ClassLoader loader) {
		validateClass(new ClassReader(bytes), loader);
	}

	static void validateClass(ClassReader reader, ClassLoader loader) {
		if (!ConfigCore.Testing.bytecodeVerify) return;

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
}
