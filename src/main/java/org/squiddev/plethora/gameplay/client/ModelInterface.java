package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;

/**
 * Model for the neural interface
 */
public class ModelInterface extends ModelBiped {
	public static final String TEXTURE = Plethora.RESOURCE_DOMAIN + ":textures/models/neural_interface.png";
	public static final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation(TEXTURE);

	private static ModelInterface normalInstance;
	private static ModelInterface monocleInstance;

	public static ModelInterface getNormal() {
		ModelInterface model = normalInstance;
		if (model == null) {
			model = normalInstance = new ModelInterface();

			ModelRenderer main = new ModelRenderer(model, 0, 0);
			main.setRotationPoint(0.0F, 0.0F, 0.0F);
			main.addBox(-0.1F, -5.5F, -5.1F, 5, 3, 1, 0.0F);

			ModelRenderer side = new ModelRenderer(model, 5, 0);
			side.setRotationPoint(0.0F, 0.0F, 0.0F);
			side.addBox(3.9F, -5.5F, -4.1F, 1, 2, 7, 0.0F);

			model.bipedHeadwear.addChild(main);
			model.bipedHeadwear.addChild(side);
		}

		return model;
	}

	public static ModelInterface getMonocle() {
		ModelInterface model = monocleInstance;
		if (model == null) {
			model = monocleInstance = new ModelInterface();

			ModelRenderer main = new ModelRenderer(model, 14, 0);
			main.setRotationPoint(0.0F, 0.0F, 0.0F);
			main.addBox(-0.1F, -5.5F, -5.1F, 3, 3, 1, 0.0F);

			model.bipedHeadwear.addChild(main);
		}

		return model;
	}

	private ModelInterface() {
		textureWidth = 22;
		textureHeight = 9;
		bipedHeadwear = new ModelRenderer(this, 0, 0);
	}

	@Override
	public void render(@Nonnull Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);

		GlStateManager.pushMatrix();
		if (entity.isSneaking()) GlStateManager.translate(0, 0.2f, 0);

		bipedHeadwear.render(f5);

		GlStateManager.popMatrix();
	}

	public static void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
