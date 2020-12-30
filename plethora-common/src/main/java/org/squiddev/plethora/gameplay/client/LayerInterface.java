package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;

import javax.annotation.Nonnull;

public class LayerInterface implements LayerRenderer<EntityLivingBase> {
	private static final float PIXEL = 0.0625f;
	private final ModelRenderer renderer;
	private final ModelInterface iface;

	private final float dx;
	private final float dy;
	private final float dz;

	private final float rx;
	private final float ry;
	private final float rz;

	public LayerInterface(ModelRenderer renderer, ModelInterface iface, float dx, float dy, float dz, float rx, float ry, float rz) {
		this.renderer = renderer;
		this.iface = iface;
		this.dx = PIXEL * dx;
		this.dy = PIXEL * dy;
		this.dz = PIXEL * dz;

		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
	}

	@Override
	public void doRenderLayer(@Nonnull EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (NeuralHelpers.getSlot(entity) == null || entity.isPotionActive(MobEffects.INVISIBILITY)) {
			return;
		}

		GlStateManager.pushMatrix();

		renderer.postRender(PIXEL);
		GlStateManager.translate(dx, dy, dz);
		GlStateManager.rotate(rx, 1, 0, 0);
		GlStateManager.rotate(ry, 0, 1, 0);
		GlStateManager.rotate(rz, 0, 0, 1);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		Minecraft.getMinecraft().getTextureManager().bindTexture(ModelInterface.TEXTURE_RESOURCE);

		ModelInterface.setRotateAngle(iface.bipedHeadwear, 0, 0, 0);
		iface.bipedHeadwear.render(PIXEL);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
