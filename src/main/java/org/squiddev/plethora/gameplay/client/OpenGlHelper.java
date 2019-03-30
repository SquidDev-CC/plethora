package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

/**
 * Some extensions to {@link net.minecraft.client.renderer.OpenGlHelper}.
 */
public class OpenGlHelper extends net.minecraft.client.renderer.OpenGlHelper {
	private static FboMode mode;
	private static int GL_DRAW_FRAMEBUFFER_BINDING = -1;

	private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);

	public static int getCurrentBuffer() {
		setup();
		return GL_DRAW_FRAMEBUFFER_BINDING == -1 ? 0 : GlStateManager.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
	}

	public static FloatBuffer getProjectionMatrix() {
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
		return projection;
	}

	public static FloatBuffer getModelViewMatrix() {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
		return modelView;
	}

	private static void setup() {
		if (mode != null) return;

		ContextCapabilities contextcapabilities = GLContext.getCapabilities();
		if (contextcapabilities.OpenGL30) {
			mode = FboMode.GL3;
			GL_DRAW_FRAMEBUFFER_BINDING = GL30.GL_DRAW_FRAMEBUFFER_BINDING;
		} else if (contextcapabilities.GL_ARB_framebuffer_object) {
			mode = FboMode.ARB;
			GL_DRAW_FRAMEBUFFER_BINDING = ARBFramebufferObject.GL_DRAW_FRAMEBUFFER_BINDING;
		} else if (contextcapabilities.GL_EXT_framebuffer_object) {
			mode = FboMode.EXT;
		} else {
			mode = FboMode.NONE;
		}
	}

	public enum FboMode {
		NONE,
		GL3,
		ARB,
		EXT,
	}
}
