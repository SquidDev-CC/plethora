package org.squiddev.plethora.gameplay.client.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.EntityLaser;

import javax.annotation.Nonnull;

public class RenderLaser extends Render<EntityLaser> {
	private static final float scale = 0.05625f;

	public RenderLaser(RenderManager manager) {
		super(manager);
	}

	@Override
	public void doRender(@Nonnull EntityLaser entity, double dx, double dy, double dz, float urm, float ticks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) dx, (float) dy, (float) dz);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * ticks - 90.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * ticks, 0.0f, 0.0f, 1.0f);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
		GlStateManager.enableAlpha();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		GlStateManager.rotate(45.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(-4.0f, 0.0f, 0.0f);

		for (int i = 0; i < 2; i++) {
			GlStateManager.rotate(90, 1, 0, 0);

			GlStateManager.color(1.0f, 0.0f, 0.0f, 0.25f);
			GL11.glLineWidth(3);
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			renderer.pos(-9, -2, 0).endVertex();
			renderer.pos(+9, -2, 0).endVertex();
			renderer.pos(+9, +2, 0).endVertex();
			renderer.pos(-9, +2, 0).endVertex();
			tessellator.draw();

			GlStateManager.color(1.0f, 0.0f, 0.0f, 0.9f);
			GL11.glLineWidth(1);
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			renderer.pos(-8, -1, 0).endVertex();
			renderer.pos(+8, -1, 0).endVertex();
			renderer.pos(+8, +1, 0).endVertex();
			renderer.pos(-8, +1, 0).endVertex();
			tessellator.draw();
		}

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
		super.doRender(entity, dx, dy, dz, urm, ticks);
	}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityLaser entityLaser) {
		return null;
	}
}
