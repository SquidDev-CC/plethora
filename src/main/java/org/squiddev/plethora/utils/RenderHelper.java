package org.squiddev.plethora.utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderHelper {
	public static final float EXPAND = 0.002f;

	public static void renderBoundingBox(EntityPlayer player, AxisAlignedBB box, BlockPos pos, double partialTicks) {
		renderBoundingBox(player, box.offset(pos.getX(), pos.getY(), pos.getZ()), partialTicks);
	}

	public static void renderBoundingBox(EntityPlayer player, AxisAlignedBB box, double partialTicks) {
		double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		doRenderBoundingBox(box.offset(-x, -y, -z));
	}

	public static void renderBoundingBox(AxisAlignedBB box) {
		doRenderBoundingBox(box.expand(EXPAND, EXPAND, EXPAND));
	}

	private static void doRenderBoundingBox(AxisAlignedBB axis) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.color(0.0f, 0.0f, 0.0f, 0.4f);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);


		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		worldrenderer.pos(axis.minX, axis.minY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.minY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.minY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.minY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.minY, axis.minZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		worldrenderer.pos(axis.minX, axis.maxY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.maxY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.maxY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.maxY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.maxY, axis.minZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		worldrenderer.pos(axis.minX, axis.minY, axis.minZ).endVertex();
		worldrenderer.pos(axis.minX, axis.maxY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.minY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.maxY, axis.minZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.minY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.maxX, axis.maxY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.minY, axis.maxZ).endVertex();
		worldrenderer.pos(axis.minX, axis.maxY, axis.maxZ).endVertex();
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
}
