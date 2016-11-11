package org.squiddev.plethora.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderHelper {
	private static final float EXPAND = 0.002f;

	public static void renderBoundingBox(EntityPlayer player, AxisAlignedBB box, BlockPos pos, double partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.color(0.0f, 0.0f, 0.0f, 0.4f);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		drawSelectionBoundingBox(box.expand(EXPAND, EXPAND, EXPAND).offset(-x + pos.getX(), -y + pos.getY(), -z + pos.getZ()));

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	private static void drawSelectionBoundingBox(AxisAlignedBB axis) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

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
	}
}
