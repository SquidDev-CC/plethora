package org.squiddev.plethora.client.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.TileManipulator;

import java.util.List;

import static org.squiddev.plethora.gameplay.BlockManipulator.OFFSET;

public final class RenderManipulator extends TileEntitySpecialRenderer<TileManipulator> {
	private ItemModelMesher mesher;

	@Override
	public void renderTileEntityAt(TileManipulator tileManipulator, double x, double y, double z, float f, int i) {
		ItemStack stack = tileManipulator.getStack();
		if (stack != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y + OFFSET, z);

			GlStateManager.translate(0.5f, 0.5f, 0.5f);
			GlStateManager.rotate((float) tileManipulator.incrementRotation(), 0f, 1f, 0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			GlStateManager.translate(-0.5f, -0.5f, -0.5f);

			renderModel(getMesher().getItemModel(stack));

			GlStateManager.popMatrix();
		}
	}

	private ItemModelMesher getMesher() {
		ItemModelMesher mesher = this.mesher;
		if (mesher == null) {
			mesher = this.mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		}
		return mesher;
	}

	private void renderModel(IBakedModel model) {
		if (model instanceof IFlexibleBakedModel) {
			this.renderModel((IFlexibleBakedModel) model);
		} else {
			this.renderModel(new IFlexibleBakedModel.Wrapper(model, DefaultVertexFormats.ITEM));
		}
	}

	private void renderModel(IFlexibleBakedModel model) {
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

		renderer.begin(GL11.GL_QUADS, model.getFormat());
		for (EnumFacing facing : EnumFacing.VALUES) {
			renderQuads(renderer, model.getFaceQuads(facing));
		}

		renderQuads(renderer, model.getGeneralQuads());
		tessellator.draw();
	}

	private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads) {
		for (BakedQuad quad : quads) {
			LightUtil.renderQuadColor(renderer, quad, -1);
		}
	}
}
