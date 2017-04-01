package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.util.List;

public class RenderHelpers {
	private static final Matrix4f identity;

	static {
		identity = new Matrix4f();
		identity.setIdentity();
	}

	public static void renderModel(IBakedModel model) {
		if (model instanceof IFlexibleBakedModel) {
			renderModel((IFlexibleBakedModel) model);
		} else {
			renderModel(new IFlexibleBakedModel.Wrapper(model, DefaultVertexFormats.ITEM));
		}
	}

	private static void renderModel(IFlexibleBakedModel model) {
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

	private static void renderQuads(WorldRenderer renderer, List<BakedQuad> quads) {
		for (BakedQuad quad : quads) {
			LightUtil.renderQuadColor(renderer, quad, -1);
		}
	}

	public static Matrix4f getIdentity() {
		return identity;
	}

	@SideOnly(Side.CLIENT)
	private static ItemModelMesher mesher;

	@SideOnly(Side.CLIENT)
	public static ItemModelMesher getMesher() {
		ItemModelMesher mesher = RenderHelpers.mesher;
		if (mesher == null) {
			mesher = RenderHelpers.mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		}
		return mesher;
	}
}
