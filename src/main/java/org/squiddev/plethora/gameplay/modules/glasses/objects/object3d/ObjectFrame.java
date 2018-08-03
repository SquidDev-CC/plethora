package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.client.FramebufferGlasses;
import org.squiddev.plethora.gameplay.client.OpenGlHelper;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.Objects;

import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.HEIGHT;
import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.WIDTH;

public class ObjectFrame extends BaseObject implements ObjectGroup.Frame2D, Positionable3D, Rotatable3D {
	private static final float SCALE = 1 / 64.0f;

	@SideOnly(Side.CLIENT)
	private static int framebufferBuffer;
	@SideOnly(Side.CLIENT)
	private static int framebufferTexture;
	@SideOnly(Side.CLIENT)
	private static int framebufferDepth;

	private Vec3d position = Vec3d.ZERO;
	private Vec3d rotation = null;

	public ObjectFrame(int id, int parent) {
		super(id, parent, ObjectRegistry.FRAME_3D);
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec3d position) {
		if (!this.position.equals(position)) {
			this.position = position;
			setDirty();
		}
	}

	@Nullable
	@Override
	public Vec3d getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(@Nullable Vec3d rotation) {
		if (!Objects.equals(this.rotation, rotation)) {
			this.rotation = rotation;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec3d(buf, position);
		if (rotation == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			ByteBufUtils.writeVec3d(buf, rotation);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec3d(buf);
		rotation = buf.readBoolean() ? ByteBufUtils.readVec3d(buf) : null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		IntSet children = canvas.getChildren(id());
		if (children == null) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		RenderManager renderManager = minecraft.getRenderManager();

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, position.z);
		GlStateManager.scale(SCALE, -SCALE, SCALE);
		if (rotation == null) {
			GlStateManager.rotate(180 - renderManager.playerViewY, 0, 1, 0);
			GlStateManager.rotate(renderManager.playerViewX, 1, 0, 0);
		} else {
			GlStateManager.rotate((float) rotation.x, 1, 0, 0);
			GlStateManager.rotate((float) rotation.y, 0, 1, 0);
			GlStateManager.rotate((float) rotation.z, 0, 0, 1);
		}

		int currentBuffer = 0;
		FloatBuffer projection = null, modelView = null;
		if (OpenGlHelper.framebufferSupported) {
			// Get the current framebuffer and restore back to that.
			// Is it grim? Yes. Is it needed? Probably.
			currentBuffer = OpenGlHelper.getCurrentBuffer();
			projection = OpenGlHelper.getProjectionMatrix();
			modelView = OpenGlHelper.getModelViewMatrix();

			FramebufferGlasses.INSTANCE.bindBuffer();
			FramebufferGlasses.INSTANCE.setupViewport();

			// Reset the buffer
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.clearColor(0, 0, 0, 0);
			GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			// Setup the projection matrix (seeEntityRenderer.setupOverlayRendering)
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, WIDTH, HEIGHT, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);
		}

		canvas.drawChildren(children.iterator());

		if (OpenGlHelper.framebufferSupported) {
			// Restore matrices
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GL11.glLoadMatrix(projection);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadMatrix(modelView);

			// And restore framebuffer
			OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, currentBuffer);
			GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

			GlStateManager.enableTexture2D();
			GlStateManager.disableCull();
			GlStateManager.enableBlend();

			// We need to discard any transparent pixels in the framebuffer
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0);

			FramebufferGlasses.INSTANCE.bindTexture();

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			buffer.pos(0, HEIGHT, 0).tex(0, 0).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(WIDTH, HEIGHT, 0).tex(1, 0).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(WIDTH, 0, 0).tex(1, 1).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();

			tessellator.draw();

			GlStateManager.bindTexture(0);
		}

		GlStateManager.popMatrix();
	}
}
