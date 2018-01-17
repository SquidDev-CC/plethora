package org.squiddev.plethora.gameplay.client.tile;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.gameplay.modules.ManipulatorType;
import org.squiddev.plethora.gameplay.modules.TileManipulator;
import org.squiddev.plethora.utils.MatrixHelpers;

import javax.vecmath.Matrix4f;

import static org.squiddev.plethora.gameplay.client.RenderHelpers.getMesher;
import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;

public final class RenderManipulator extends TileEntitySpecialRenderer<TileManipulator> {
	@Override
	public void renderTileEntityAt(TileManipulator manipulator, double x, double y, double z, float f, int j) {
		GlStateManager.pushMatrix();

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		GlStateManager.translate(x, y, z);
		ForgeHooksClient.multiplyCurrentGlMatrix(MatrixHelpers.matrixFor(manipulator.getFacing()));
		GlStateManager.translate(0, OFFSET, 0);

		ManipulatorType type = manipulator.getType();
		float delta = (float) manipulator.incrementRotation();

		int size = type.size();
		AxisAlignedBB[] boxes = type.boxesFor(EnumFacing.DOWN);
		for (int i = 0; i < size; i++) {
			ItemStack stack = manipulator.getStack(i);
			if (stack != null) {
				GlStateManager.pushMatrix();

				AxisAlignedBB box = boxes[i];
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

				RenderHelpers.renderModel(model);

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
