package org.squiddev.plethora.gameplay.client.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.gameplay.modules.ManipulatorType;
import org.squiddev.plethora.gameplay.modules.TileManipulator;

import javax.vecmath.Matrix4f;
import java.util.List;

import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;
import static org.squiddev.plethora.utils.Helpers.getMesher;

public final class RenderManipulator extends TileEntitySpecialRenderer<TileManipulator> {
	@Override
	public void renderTileEntityAt(TileManipulator tileManipulator, double x, double y, double z, float f, int j) {
		GlStateManager.pushMatrix();

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		GlStateManager.translate(x, y + OFFSET, z);

		ManipulatorType type = tileManipulator.getType();

		float delta = (float) tileManipulator.incrementRotation();

		int size = type.size();
		for (int i = 0; i < size; i++) {
			ItemStack stack = tileManipulator.getStack(i);
			if (stack != null) {
				GlStateManager.pushMatrix();

				AxisAlignedBB box = type.boxes[i];

				GlStateManager.translate(
					(box.minX + box.maxX) / 2.0f,
					type.scale,
					(box.minZ + box.maxZ) / 2.0f
				);


				IBakedModel model;
				IModuleHandler handler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
				if (handler != null) {
					Pair<IBakedModel, Matrix4f> pair = handler.getModel(delta);
					ForgeHooksClient.multiplyCurrentGlMatrix(pair.getRight());
					model = pair.getLeft();
				} else {
					GlStateManager.rotate(delta, 0f, 1f, 0f);
					model = getMesher().getModelManager().getMissingModel();
				}

				GlStateManager.scale(type.scale, type.scale, type.scale);
				GlStateManager.translate(-0.5f, -0.7f, -0.5f);

				renderModel(model);

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
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
