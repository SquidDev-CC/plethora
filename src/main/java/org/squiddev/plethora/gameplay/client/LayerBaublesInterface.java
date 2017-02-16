package org.squiddev.plethora.gameplay.client;

import baubles.api.BaublesApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.gameplay.registry.Registry;

public class LayerBaublesInterface implements LayerRenderer<EntityPlayer> {
	private final ModelPlayer model;
	private final static float PIXEL = 0.0625f;

	public LayerBaublesInterface(ModelPlayer model) {
		this.model = model;
	}

	@Override
	public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (player.isPotionActive(Potion.invisibility)) {
			return;
		}

		ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(NeuralHelpers.BAUBLES_SLOT);
		if (stack == null || stack.getItem() != Registry.itemNeuralInterface) return;

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();

		model.bipedHeadwear.postRender(PIXEL);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		if (player.getEquipmentInSlot(NeuralHelpers.ARMOR_SLOT) != null) {
			GlStateManager.translate(0.05f, 0, -0.05f);
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(ModelInterface.TEXTURE_RESOURCE);

		ModelInterface iface = ModelInterface.getNormal();
		ModelInterface.setRotateAngle(iface.bipedHeadwear, 0, 0, 0);
		iface.bipedHeadwear.render(PIXEL);

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
