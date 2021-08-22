package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.client.FramebufferGlasses;
import org.squiddev.plethora.gameplay.client.OpenGlHelper;
import org.squiddev.plethora.gameplay.client.RenderState;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ItemObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.NBTTaggable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.HEIGHT;
import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.WIDTH;

public class Item2D extends BaseObject implements NBTTaggable, Scalable, ItemObject, Positionable2D {
	private float scale;
	private Vec2d position = Vec2d.ZERO;

	private int damage;
	private NBTTagCompound nbt;
	private Item item;
	private ItemStack stack;

	public Item2D(int id, int parent) {
		super(id, parent, ObjectRegistry.ITEM_2D);
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		if (this.scale != scale) {
			this.scale = scale;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec2d position) {
		if (!this.position.equals(position)) {
			this.position = position;
			setDirty();
		}
	}

	@Override
	public int getDamage() {
		return damage;
	}

	@Override
	public void setDamage(int damage) {
		if (this.damage != damage) {
			this.damage = damage;
			stack = null;
			setDirty();
		}
	}

	@Override
	public void setNBTTagCompound(NBTTagCompound nbt) {
		if (this.nbt == null || this.nbt != nbt) {
			this.nbt = nbt;
			stack = null;
			setDirty();
		}
	}

	@Override
	public NBTTagCompound getNBTTagCompound() { return nbt;	}

	@Override
	@Nonnull
	public Item getItem() {
		return item;
	}

	@Override
	public void setItem(@Nonnull Item item) {
		if (this.item != item) {
			this.item = item;
			stack = null;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec2d(buf, position);
		buf.writeFloat(scale);
		ByteBufUtils.writeUTF8String(buf, item.getRegistryName().toString());
		buf.writeInt(damage);
		ByteBufUtils.writeTag(buf, nbt);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec2d(buf);
		scale = buf.readFloat();

		ResourceLocation name = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		item = Item.REGISTRY.getObject(name);

		damage = buf.readInt();

		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {

		RenderState state = RenderState.get();
		if (OpenGlHelper.framebufferSupported) {
			FramebufferGlasses.ITEM_2D.bindBuffer();
			FramebufferGlasses.clear();
			FramebufferGlasses.ITEM_2D.setupViewport();

			// Setup the projection matrix (seeEntityRenderer.setupOverlayRendering)
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, WIDTH, HEIGHT, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);
		}

		// Actually render the thing
		GlStateManager.pushMatrix();

		GlStateManager.translate(position.x, position.y, 0);
		GlStateManager.scale(scale, scale, 1);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableDepth();

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderHelper.enableGUIStandardItemLighting();

		if (stack == null) {
			stack = new ItemStack(item, 1, damage, nbt);
			stack.setTagCompound(nbt);
		}
		Minecraft.getMinecraft().getRenderItem()
			.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, stack, 0, 0);

		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();

		if (OpenGlHelper.framebufferSupported) {
			state.restore();

			GlStateManager.enableTexture2D();
			GlStateManager.enableBlend();

			// We need to discard any transparent pixels in the framebuffer
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0f);

			FramebufferGlasses.ITEM_2D.bindTexture();

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			buffer.pos(0, HEIGHT, 0).tex(0, 0).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(WIDTH, HEIGHT, 0).tex(1, 0).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(WIDTH, 0, 0).tex(1, 1).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();

			tessellator.draw();

			GlStateManager.bindTexture(0);
		}
	}
}
