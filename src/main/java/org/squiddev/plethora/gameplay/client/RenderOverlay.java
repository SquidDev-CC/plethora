package org.squiddev.plethora.gameplay.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.gameplay.registry.Registry;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.squiddev.plethora.gameplay.ConfigGameplay.Scanner;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Sensor;
import static org.squiddev.plethora.gameplay.modules.ChatVisualiser.ChatMessage;

/**
 * Renders overlays for various modules
 */
public class RenderOverlay extends Module implements IClientModule {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Plethora.RESOURCE_DOMAIN, "textures/misc/flare.png");

	private int ticks;

	private static final LinkedList<ChatMessage> chatMessages = new LinkedList<ChatMessage>();

	@SideOnly(Side.CLIENT)
	public static void addMessage(ChatMessage message) {
		chatMessages.add(message);
	}

	public static void clearChatMessages() {
		chatMessages.clear();
	}

	private static final class BlockStack {
		public final Block block;
		public final int meta;

		private BlockStack(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BlockStack)) return false;

			BlockStack that = (BlockStack) o;
			return meta == that.meta && block.equals(that.block);
		}

		@Override
		public int hashCode() {
			int result = block.hashCode();
			result = 31 * result + meta;
			return result;
		}
	}

	private static final Map<BlockStack, Boolean> oreBlockCache = new HashMap<BlockStack, Boolean>();

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderOverlay(RenderWorldLastEvent event) {
		ticks += 1;
		if (ticks > Math.PI * 2 * 1000) ticks = 0;

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.thePlayer;
		for (EnumHand hand : EnumHand.values()) {
			renderOverlay(event, player.getHeldItem(hand));
		}
	}

	@SideOnly(Side.CLIENT)
	private void renderOverlay(RenderWorldLastEvent event, ItemStack stack) {
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.thePlayer;
		World world = player.worldObj;

		// "Tick" each iterator and remove them.
		for (Iterator<ChatMessage> messages = chatMessages.iterator(); messages.hasNext(); ) {
			if (messages.next().decrement()) {
				messages.remove();
			}
		}

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
					Vec3d position = player.getPositionEyes(event.getPartialTicks());
					List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
						position.xCoord - Sensor.radius, position.yCoord - Sensor.radius, position.zCoord - Sensor.radius,
						position.xCoord + Sensor.radius, position.yCoord + Sensor.radius, position.zCoord + Sensor.radius
					));

					for (EntityLivingBase entity : entities) {
						if (entity != minecraft.thePlayer) {
							renderFlare(
								entity.posX, entity.posY + (entity.height / 2), entity.posZ,
								entity.getEntityId(), 1.0f, renderManager
							);
						}
					}
					break;
				}
				case ItemModule.SCANNER_ID: {
					// Try to find all ore blocks and render them
					BlockPos pos = player.getPosition();
					final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					for (int oX = x - Scanner.radius; oX <= x + Scanner.radius; oX++) {
						for (int oY = y - Scanner.radius; oY <= y + Scanner.radius; oY++) {
							for (int oZ = z - Scanner.radius; oZ <= z + Scanner.radius; oZ++) {
								IBlockState state = world.getBlockState(new BlockPos(oX, oY, oZ));
								Block block = state.getBlock();

								if (isBlockOre(block, block.getMetaFromState(state))) {
									renderFlare(oX + 0.5, oY + 0.5, oZ + 0.5, block.getRegistryName().hashCode(), 1.0f, renderManager);
								}
							}
						}
					}
					break;
				}
				case ItemModule.CHAT_ID: {
					for (ChatMessage message : chatMessages) {
						if (message.getWorld() == world.provider.getDimensionId()) {
							Vec3 pos = message.getPosition();
							renderFlare(
								pos.xCoord, pos.yCoord, pos.zCoord,
								message.getId(), message.getCount() * 2.0f / ChatMessage.TIME, renderManager
							);

							// TODO: Display chat too.
						}
					}
				}
			}

			GlStateManager.popMatrix();

			GlStateManager.enableDepth();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
		}
	}

	private static boolean isBlockOre(Block block, int meta) {
		if (block == null) {
			return false;
		}

		if (block instanceof BlockOre || block instanceof BlockRedstoneOre) {
			return true;
		}

		if (Item.getItemFromBlock(block) == null) {
			return false;
		}

		BlockStack type = new BlockStack(block, meta);
		Boolean cached = oreBlockCache.get(type);
		if (cached != null) return cached;

		ItemStack stack = new ItemStack(block, meta);
		for (int id : OreDictionary.getOreIDs(stack)) {
			String oreName = OreDictionary.getOreName(id);
			if (oreName.contains("ore")) {
				oreBlockCache.put(type, true);
				return true;
			}
		}

		oreBlockCache.put(type, false);
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void renderFlare(double x, double y, double z, int id, float size, RenderManager manager) {
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
		size *= 0.2f + MathHelper.sin(ticks / 100.0f + offset) / 16.0f;

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

	@SideOnly(Side.CLIENT)
	private void renderQuad(Tessellator tessellator, float size) {
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(-size, -size, 0).tex(0, 1).endVertex();
		buffer.pos(-size, +size, 0).tex(1, 1).endVertex();
		buffer.pos(+size, +size, 0).tex(1, 0).endVertex();
		buffer.pos(+size, -size, 0).tex(0, 0).endVertex();

		tessellator.draw();
	}
}
