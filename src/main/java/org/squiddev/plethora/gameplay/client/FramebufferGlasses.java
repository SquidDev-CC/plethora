package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.squiddev.plethora.gameplay.ConfigGameplay;

import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.HEIGHT;
import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.WIDTH;

/**
 * A wrapper for constructing and manipulating framebuffers
 *
 * This is very similar to Minecraft's {@link net.minecraft.client.shader.Framebuffer}, it just avoids
 * some of the Minecraft specific things (such as config options).
 */
public class FramebufferGlasses {
	public static final FramebufferGlasses INSTANCE = new FramebufferGlasses();

	private int buffer = -1;
	private int texture;
	private int depth;

	private int textureWidth;
	private int textureHeight;

	public void dispose() {
		if (buffer == -1) return;

		OpenGlHelper.glDeleteRenderbuffers(depth);
		TextureUtil.deleteTexture(texture);
		OpenGlHelper.glDeleteFramebuffers(buffer);
		buffer = -1;
	}

	private void createBuffer() {
		int scale = Math.max(1, ConfigGameplay.Glasses.framebufferScale);
		int textureWidth = WIDTH * scale, textureHeight = HEIGHT * scale;

		// If we have a buffer and the dimensions are the same then just use that.
		if (buffer != -1 && textureWidth == this.textureWidth && textureHeight == this.textureHeight) return;

		dispose();

		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		// Bind framebuffer
		buffer = OpenGlHelper.glGenFramebuffers();
		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, buffer);

		// Bind texture
		texture = TextureUtil.glGenTextures();

		GlStateManager.bindTexture(texture);
		GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GlStateManager.bindTexture(0);

		OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);

		// Bind depth
		depth = OpenGlHelper.glGenRenderbuffers();

		OpenGlHelper.glBindRenderbuffer(OpenGlHelper.GL_RENDERBUFFER, depth);
		OpenGlHelper.glRenderbufferStorage(OpenGlHelper.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, textureWidth, textureHeight);
		OpenGlHelper.glBindRenderbuffer(OpenGlHelper.GL_RENDERBUFFER, 0);

		OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, depth);

		int status = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);
		if (status != OpenGlHelper.GL_FRAMEBUFFER_COMPLETE) {
			if (status == OpenGlHelper.GL_FB_INCOMPLETE_ATTACHMENT) {
				throw new IllegalStateException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			} else if (status == OpenGlHelper.GL_FB_INCOMPLETE_MISS_ATTACH) {
				throw new IllegalStateException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			} else if (status == OpenGlHelper.GL_FB_INCOMPLETE_DRAW_BUFFER) {
				throw new IllegalStateException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			} else if (status == OpenGlHelper.GL_FB_INCOMPLETE_READ_BUFFER) {
				throw new IllegalStateException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			} else {
				throw new IllegalStateException("glCheckFramebufferStatus returned unknown status:" + status);
			}
		}
	}

	public void bindBuffer() {
		createBuffer();
		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, buffer);
	}

	public void setupViewport() {
		if (buffer == -1) throw new IllegalStateException("Buffer has been disposed");
		GlStateManager.viewport(0, 0, textureWidth, textureHeight);
	}

	public void bindTexture() {
		if (buffer == -1) throw new IllegalStateException("Buffer has been disposed");
		GlStateManager.bindTexture(texture);
	}
}
