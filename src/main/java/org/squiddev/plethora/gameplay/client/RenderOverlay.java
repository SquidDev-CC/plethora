package org.squiddev.plethora.gameplay.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.integration.registry.IClientModule;
import org.squiddev.plethora.integration.registry.Module;
import org.squiddev.plethora.integration.registry.Registry;

import java.awt.*;
import java.util.List;

import static org.squiddev.plethora.gameplay.modules.ItemModule.SCANNER_RADIUS;
import static org.squiddev.plethora.gameplay.modules.ItemModule.SENSOR_RADIUS;

/**
 * Renders overlays for various modules
 */
public class RenderOverlay extends Module implements IClientModule {
	public static ResourceLocation TEXTURE = new ResourceLocation(Plethora.RESOURCE_DOMAIN, "textures/misc/flare.png");
	private int ticks;

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderOverlay(RenderWorldLastEvent event) {
		ticks += 1;
		if (ticks > Math.PI * 2 * 1000) ticks = 0;

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.thePlayer;
		ItemStack stack = player.getHeldItem();
		World world = player.worldObj;

		if (stack != null && stack.getItem() == Registry.itemModule) {
			minecraft.getTextureManager().bindTexture(TEXTURE);

			GlStateManager.disableDepth();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableTexture2D();
			GlStateManager.disableCull();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);

			GlStateManager.pushMatrix();

			RenderManager renderManager = minecraft.getRenderManager();
			GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

			switch (stack.getItemDamage()) {
				case ItemModule.SENSOR_ID: {
					// Gather all entities and render them
					Vec3 position = player.getPositionEyes(event.partialTicks);
					List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
						position.xCoord - SENSOR_RADIUS, position.yCoord - SENSOR_RADIUS, position.zCoord - SENSOR_RADIUS,
						position.xCoord + SENSOR_RADIUS, position.yCoord + SENSOR_RADIUS, position.zCoord + SENSOR_RADIUS
					));

					for (EntityLivingBase entity : entities) {
						if (entity != minecraft.thePlayer) {
							renderFlare(
								entity.posX, entity.posY + (entity.height / 2), entity.posZ,
								entity.getEntityId(), renderManager
							);
						}
					}
					break;
				}
				case ItemModule.SCANNER_ID: {
					// Try to find all ore blocks and render them
					BlockPos pos = player.getPosition();
					final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					for (int oX = x - SCANNER_RADIUS; oX <= x + SCANNER_RADIUS; oX++) {
						for (int oY = y - SCANNER_RADIUS; oY <= y + SCANNER_RADIUS; oY++) {
							for (int oZ = z - SCANNER_RADIUS; oZ <= z + SCANNER_RADIUS; oZ++) {
								Block block = world.getBlockState(new BlockPos(oX, oY, oZ)).getBlock();
								String name = block.getRegistryName();

								// Nobody said it was decent
								if (name.contains("ore")) {
									renderFlare(oX + 0.5, oY + 0.5, oZ + 0.5, name.hashCode(), renderManager);
								}
							}
						}
					}
					break;
				}
			}

			GlStateManager.popMatrix();

			GlStateManager.enableDepth();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
		}
	}

	@SideOnly(Side.CLIENT)
	private void renderFlare(double x, double y, double z, int id, RenderManager manager) {
		// Generate an offset based off the hash code
		float offset = (float) (id % (Math.PI * 2));

		GlStateManager.pushMatrix();

		// Setup the view
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(-manager.playerViewY, 0, 1, 0);
		GlStateManager.rotate(manager.playerViewX, 1, 0, 0);

		// Choose a colour from the hash code
		// this isn't very fancy but it generally works
		Color color = new Color(Color.HSBtoRGB(
			MathHelper.sin(offset) / 2.0f + 0.5f,
			MathHelper.cos(offset) / 2.0f + 0.5f,
			1.0f
		));

		// The size is function of ticks and the id: ensures slightly different sizes
		float size = 0.2f + MathHelper.sin(ticks / 100.0f + offset) / 16.0f;

		// Prepare to render
		Tessellator tessellator = Tessellator.getInstance();

		// Inner highlight
		GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.5f);
		renderQuad(tessellator, size);

		// Outer aura
		GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.2f);
		renderQuad(tessellator, size * 2);

		GlStateManager.popMatrix();
	}

	private void renderQuad(Tessellator tessellator, float size) {
		WorldRenderer buffer = tessellator.getWorldRenderer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(-size, -size, 0).tex(0, 1).endVertex();
		buffer.pos(-size, +size, 0).tex(1, 1).endVertex();
		buffer.pos(+size, +size, 0).tex(1, 0).endVertex();
		buffer.pos(+size, -size, 0).tex(0, 0).endVertex();

		tessellator.draw();
	}
}
