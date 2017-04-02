package org.squiddev.plethora.gameplay.client.tile;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.gameplay.modules.ManipulatorType;
import org.squiddev.plethora.gameplay.modules.TileManipulator;

import javax.vecmath.Matrix4f;

import static org.squiddev.plethora.gameplay.client.RenderHelpers.getMesher;
import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;

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
				GlStateManager.translate(0, -0.2, 0);

				RenderHelpers.renderModel(model);

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
