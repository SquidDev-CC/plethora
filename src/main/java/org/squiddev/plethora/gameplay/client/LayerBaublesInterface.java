package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;

public class LayerBaublesInterface implements LayerRenderer<EntityPlayer> {
	@Override
	public void doRenderLayer(@Nonnull EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (player.isPotionActive(MobEffects.INVISIBILITY)) {
			return;
		}

		TinySlot stack = NeuralHelpers.getBauble(player);
		if (stack == null) return;

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ModelInterface.TEXTURE_RESOURCE);

		ModelInterface iface = ModelInterface.getNormal();
		iface.isSneak = player.isSneaking();
		iface.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
