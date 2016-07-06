package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;

public class LayerInterface implements LayerRenderer<EntityLivingBase> {
	private final static float PIXEL = 0.0625f;
	private final ModelRenderer renderer;

	private final float dx;
	private final float dy;
	private final float dz;

	public LayerInterface(ModelRenderer renderer, float dx, float dy, float dz) {
		this.renderer = renderer;
		this.dx = PIXEL * dx;
		this.dy = PIXEL * dy;
		this.dz = PIXEL * dz;
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (NeuralHelpers.getStack(entity) == null || entity.isPotionActive(Potion.invisibility)) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();

		renderer.postRender(PIXEL);
		GlStateManager.translate(dx, dy, dz);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		Minecraft.getMinecraft().getTextureManager().bindTexture(ModelInterface.TEXTURE_RESOURCE);

		ModelInterface model = ModelInterface.get();
		ModelInterface.setRotateAngle(model.bipedHeadwear, 0, 0, 0);
		model.bipedHeadwear.render(PIXEL);

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
