package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Textable;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TEXT_2D;

public class Text extends ColourableObject implements Positionable2D, Scalable, Textable {
	private Point2D position = new Point2D();
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
	public Point2D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point2D position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
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
		super.writeInital(buf);
		position.write(buf);
		buf.writeFloat(size);
		ByteBufUtils.writeUTF8String(buf, text);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position.read(buf);
		size = buf.readFloat();
		text = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void draw2D() {
		int colour = getColour();

		// If the alpha channel doesn't match a 0xFC, then the font renderer
		// will make it opaque. We also early exit here if we're transparent.
		int alpha = colour & 0xFF;
		if (alpha == 0) return;
		if ((alpha & 0xFC) == 0) colour |= 0x4;

		GlStateManager.enableTexture2D();

		FontRenderer fontrenderer = Minecraft.getMinecraft().getRenderManager().getFontRenderer();

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, 0);
		GlStateManager.scale(size, size, 1);
		// We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
		fontrenderer.drawString(text, 0, 0, Integer.rotateRight(colour, 8));
		GlStateManager.popMatrix();

		GlStateManager.disableTexture2D();
	}
}
