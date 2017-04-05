package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Textable;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TEXT_2D;

public class Text extends BaseObject implements Colourable, Positionable2D, Scalable, Textable {
	private int colour = DEFAULT_COLOUR;
	private float x;
	private float y;
	private float size = 1;
	private String text = "";

	public Text(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return TEXT_2D;
	}

	@Override
	public int getColour() {
		return colour;
	}

	@Override
	public void setColour(int colour) {
		if (this.colour != colour) {
			this.colour = colour;
			setDirty();
		}
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setPosition(float x, float y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			setDirty();
		}
	}

	@Override
	public float getScale() {
		return size;
	}

	@Override
	public void setScale(float scale) {
		if (this.size != scale) {
			this.size = scale;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(@Nonnull String text) {
		if (!this.text.equals(text)) {
			this.text = text;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeFloat(x);
		buf.writeFloat(y);
		buf.writeFloat(size);
		ByteBufUtils.writeUTF8String(buf, text);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		x = buf.readFloat();
		y = buf.readFloat();
		size = buf.readFloat();
		text = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		int colour = this.colour;

		// If the alpha channel doesn't match a 0xFC, then the font renderer
		// will make it opaque. We also early exit here if we're transparent.
		int alpha = colour & 0xFF;
		if (alpha == 0) return;
		if ((alpha & 0xFC) == 0) colour |= 0x4;

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		FontRenderer fontrenderer = Minecraft.getMinecraft().getRenderManager().getFontRenderer();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(size, size, 1);
		// We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
		fontrenderer.drawString(text, 0, 0, Integer.rotateRight(colour, 8));
		GlStateManager.popMatrix();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
