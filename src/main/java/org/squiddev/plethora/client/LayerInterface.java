package org.squiddev.plethora.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

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
		// if (NeuralHelpers.getStack(entity) != null || entity.isPotionActive(Potion.invisibility)) return;
		if (entity.isChild()) return;

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();

		renderer.postRender(PIXEL);
		GlStateManager.translate(dx, dy, dz);

		Minecraft.getMinecraft().getTextureManager().bindTexture(ModelInterface.TEXTURE_RESOURCE);
		ModelInterface.get().bipedHeadwear.render(PIXEL);

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
