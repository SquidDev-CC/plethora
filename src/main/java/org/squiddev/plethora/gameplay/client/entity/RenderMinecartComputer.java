package org.squiddev.plethora.gameplay.client.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

import static org.squiddev.plethora.gameplay.client.RenderHelpers.getMesher;

public class RenderMinecartComputer extends RenderMinecart<EntityMinecartComputer> {
	public RenderMinecartComputer(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public void doRender(@Nonnull EntityMinecartComputer entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		bindEntityTexture(entity);

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		long id = (long) entity.getEntityId() * 493286711L;
		id = id * id * 4392167121L + id * 98761L;
		float dx = (((float) (id >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
		float dy = (((float) (id >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
		float dz = (((float) (id >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
		GlStateManager.translate(dx, dy, dz);

		double ox = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
		double oy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
		double oz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
		Vec3d pos = entity.getPos(ox, oy, oz);
		float f3 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

		if (pos != null) {
			Vec3d posOffA = entity.getPosOffset(ox, oy, oz, 0.3);
			Vec3d posOffB = entity.getPosOffset(ox, oy, oz, -0.3);

			if (posOffA == null) posOffA = pos;
			if (posOffB == null) posOffB = pos;

			x += pos.xCoord - ox;
			y += (posOffA.yCoord + posOffB.yCoord) / 2.0D - oy;
			z += pos.zCoord - oz;
			Vec3d posOff = posOffB.addVector(-posOffA.xCoord, -posOffA.yCoord, -posOffA.zCoord);

			if (posOff.lengthVector() != 0.0D) {
				posOff = posOff.normalize();
				entityYaw = (float) (Math.atan2(posOff.zCoord, posOff.xCoord) * 180 / Math.PI);
				f3 = (float) (Math.atan(posOff.yCoord) * 73);
			}
		}

		GlStateManager.translate((float) x, (float) y + 0.375f, (float) z);
		GlStateManager.rotate(180.0f - entityYaw, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-f3, 0.0f, 0.0f, 1.0f);

		float roll = (float) entity.getRollingAmplitude() - partialTicks;
		float damage = entity.getDamage() - partialTicks;

		if (damage < 0.0f) damage = 0.0f;
		if (roll > 0.0f) {
			GlStateManager.rotate(MathHelper.sin(roll) * roll * damage / 10.0f * (float) entity.getRollingDirection(), 1.0f, 0.0f, 0.0f);
		}

		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		// Render block
		GlStateManager.pushMatrix();
		int tileOffset = entity.getDisplayTileOffset();
		IBlockState blockState = entity.getDisplayTile();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		final float scale = 0.75f;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(-0.5f, (float) (tileOffset - 8) / 16.0f, 0.5f);
		renderCartContents(entity, partialTicks, blockState);

		IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				GlStateManager.pushMatrix();
				switch (slot) {
					case 0: // Top
						GlStateManager.translate(-0.5f, 1.5f, -0.5f);
						GlStateManager.rotate(-90, 1, 0, 0);

						// And orient the right way
						GlStateManager.translate(0.5f, 0.5f, 0.5f);
						GlStateManager.rotate(90, 0, 0, 1);
						GlStateManager.translate(-0.5f, -0.5f, -0.5f);
						break;
					case 1: // Left
						GlStateManager.translate(0.5f, 0.5f, -1.5f);
						GlStateManager.rotate(180, 0, 1, 0);
						break;
					case 2: // Right
						GlStateManager.translate(0.5f, 0.5f, 0.5f);
						break;
					case 3: // Back
						GlStateManager.translate(1.5f, 0.5f, -0.5f);
						GlStateManager.rotate(90, 0, 1, 0);
						break;
				}

				IVehicleUpgradeHandler upgrade = stack.getCapability(Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY, null);
				IBakedModel model;
				if (upgrade == null) {
					model = getMesher().getModelManager().getMissingModel();
				} else {
					Pair<IBakedModel, Matrix4f> pair = upgrade.getModel(entity.getAccess(slot));
					ForgeHooksClient.multiplyCurrentGlMatrix(pair.getRight());
					model = pair.getLeft();
				}

				GlStateManager.translate(-0.5f, -0.5f, -0.5f);
				RenderHelpers.renderModel(model);

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.popMatrix();

		GlStateManager.disableBlend();

		// Render main minecart
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		bindEntityTexture(entity);
		GlStateManager.scale(-1.0f, -1.0f, 1.0f);
		modelMinecart.render(entity, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);

		GlStateManager.popMatrix();

		if (this.renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		this.renderName(entity, x, y, z);
	}
}
