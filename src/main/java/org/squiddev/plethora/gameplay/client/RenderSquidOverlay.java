package org.squiddev.plethora.gameplay.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.gameplay.registry.Module;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class RenderSquidOverlay extends Module implements IClientModule {
	private static final UUID uuid = UUID.fromString("d3156e4b-c712-4fd3-87b0-b24b8ca94209");

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		if (Loader.isModLoaded(CCTweaks.ID)) return;

		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		RenderLayer layer = new RenderLayer();
		skinMap.get("default").addLayer(layer);
		skinMap.get("slim").addLayer(layer);
	}

	@SideOnly(Side.CLIENT)
	private static final class RenderLayer implements LayerRenderer<EntityPlayer> {
		private static final int SEGMENTS = 5;
		private static final int TENTACLES = 6;
		private static final int BASE_ANGLE = 25;

		// Dimensions of the one tentacle
		private static final float LENGTH = 0.3f;
		private static final float WIDTH = 0.15f;

		private static final double EASING_TICKS = 5;
		private static final double OFFSET_SPEED = 0.1;
		private static final double OFFSET_VARIANCE = 7;

		private final double[] lastAngles = new double[TENTACLES * SEGMENTS];
		private final double[] offsets = new double[TENTACLES * SEGMENTS];

		private double tick = 0;

		public RenderLayer() {
			for (int i = 0; i < lastAngles.length; i++) {
				lastAngles[i] = BASE_ANGLE;
				offsets[i] = Math.random() * Math.PI * 2;
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void doRenderLayer(@Nonnull EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			GameProfile profile = player.getGameProfile(); // profile == null || !profile.getId().equals(uuid) ||
			if (!ConfigGameplay.Miscellaneous.funRender) return;

			GlStateManager.disableLighting();
			GlStateManager.disableTexture2D();

			GlStateManager.pushMatrix();
			if (player.isSneaking()) {
				GlStateManager.translate(0F, 0.2F, 0F);
				GlStateManager.rotate(90F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
			}

			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(0, 0.1, -0.3);

			GlStateManager.color(0, 0, 0, 1);

			final double angle;
			if (player.hurtTime > 0) {
				double progress = (double) player.hurtTime / player.maxHurtTime;
				angle = BASE_ANGLE - (progress * (BASE_ANGLE - OFFSET_VARIANCE));
			} else {
				double velocity = new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ).distanceTo(
					new Vec3d(player.posX, player.posY, player.posZ));

				double adjusted = 1 - Math.exp(velocity * -2);

				angle = BASE_ANGLE - adjusted * BASE_ANGLE;
			}


//			DebugLogger.info(new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ).distanceTo(
//				new Vec3d(player.posX, player.posY, player.posZ)) + " !");

			tick = (tick + partialTicks) % (Math.PI * 2 / OFFSET_SPEED);

			for (int i = 0; i < TENTACLES; i++) {
				GlStateManager.pushMatrix();

				GlStateManager.rotate(360.0f / TENTACLES * i, 0, 1, 0);
				GlStateManager.translate(0.1, 0, 0);

				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer renderer = tessellator.getBuffer();

				for (int j = 0; j < SEGMENTS; j++) {
					// Offset each tentacle by a random amount
					double lastAngle = lastAngles[i * SEGMENTS + j];
					double thisAngle = angle + Math.sin(offsets[i * SEGMENTS + j] + tick * OFFSET_SPEED) * OFFSET_VARIANCE;

					// Angle each tentacle to get a "claw" effect.
					thisAngle *= Math.cos(j * (Math.PI / (SEGMENTS - 1)));

					// Provide some basic easing on the angle
					// Basically the middle segments have a high "delta"
					if (Math.abs(lastAngle - thisAngle) > 1) {
						thisAngle = lastAngle - (lastAngle - thisAngle) / EASING_TICKS;
					}

					lastAngles[i * SEGMENTS + j] = thisAngle;

					GlStateManager.rotate((float) thisAngle, 0, 0, -1);

					renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					tentacle(renderer);
					tessellator.draw();

					GlStateManager.translate(0, LENGTH - WIDTH / 2, 0);
				}

				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();

			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean shouldCombineTextures() {
			return false;
		}

		private static void tentacle(VertexBuffer renderer) {
			renderer.pos(0, 0, -WIDTH / 2).endVertex();
			renderer.pos(0, 0, WIDTH / 2).endVertex();
			renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();

			renderer.pos(0, 0, -WIDTH / 2).endVertex();
			renderer.pos(0, 0, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();

			renderer.pos(0, 0, -WIDTH / 2).endVertex();
			renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();
			renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();

			renderer.pos(WIDTH, 0, -WIDTH / 2).endVertex();
			renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();

			renderer.pos(0, LENGTH, -WIDTH / 2).endVertex();
			renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, -WIDTH / 2).endVertex();

			renderer.pos(0, 0, WIDTH / 2).endVertex();
			renderer.pos(0, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, LENGTH, WIDTH / 2).endVertex();
			renderer.pos(WIDTH, 0, WIDTH / 2).endVertex();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
