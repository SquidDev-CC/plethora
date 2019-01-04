package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class RenderState {
	private static final Queue<RenderState> available = new ArrayDeque<>();

	private int buffer;
	private final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
	private final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
	private final IntBuffer viewport = BufferUtils.createIntBuffer(16);

	private void setup() {
		buffer = OpenGlHelper.getCurrentBuffer();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
	}

	public void restore() {
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GL11.glLoadMatrix(projection);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadMatrix(modelView);

		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, buffer);
		GL11.glViewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));

		available.add(this);
	}

	public static RenderState get() {
		RenderState next = available.poll();
		if (next == null) next = new RenderState();

		next.setup();
		return next;
	}
}
