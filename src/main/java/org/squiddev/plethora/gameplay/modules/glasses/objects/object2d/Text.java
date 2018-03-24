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

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Text extends ColourableObject implements Positionable2D, Scalable, Textable {
	private Point2D position = new Point2D();
	private float size = 1;
	private String text = "";

	// We use a two dimensional string array to indicate where tabs are.
	// For example, "Hello\tworld\nFoo\tBar" would become {{"Hello", "world"}, {"Foo", "Bar"}}
	// This is used in the rendering to simulate tabs.
	private static final String[][] EMPTY_LINES = {};
	// A tab is 4 spaces and one space is 4 pixels wide -> 1 tab is 4*4 (16) pixels wide. Used during rendering
	private static final int TAB_WIDTH = 16;
	private String[][] lines = EMPTY_LINES;
	private boolean dropShadow = false;

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
			lines = splitText(text);
			setDirty();
		}
	}
	
	@Override
	public void setShadow(boolean dropShadow) {
		if (this.dropShadow == dropShadow) return;
		this.dropShadow = dropShadow;
		setDirty();
	}
	
	@Override
	public boolean hasShadow() {
		return dropShadow;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		super.writeInital(buf);
		position.write(buf);
		buf.writeFloat(size);
		buf.writeBoolean(dropShadow);
		ByteBufUtils.writeUTF8String(buf, text);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position.read(buf);
		size = buf.readFloat();
		dropShadow = buf.readBoolean();
		text = ByteBufUtils.readUTF8String(buf);
		lines = splitText(text);
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

		int x = 0;
		int y = 0;
		for (String[] fullLine : lines) {
			for (String tabSection : fullLine) {
				// We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
				if (dropShadow)
					x = fontrenderer.drawStringWithShadow(tabSection, x, y, Integer.rotateRight(colour, 8));
				else
					x = fontrenderer.drawString(tabSection, x, y, Integer.rotateRight(colour, 8));
				x = (int) Math.floor(x/TAB_WIDTH)*TAB_WIDTH+TAB_WIDTH;
			}
			// Carriage return
			x = 0;
			// Set x to the next tab location
			y += fontrenderer.FONT_HEIGHT;
		}

		GlStateManager.popMatrix();
	}

	private String[][] splitText(String text) {
		String[] lines = text.split("\n|\r");
		return Arrays.stream(lines).map(str -> str.split("\t")).toArray(String[][]::new);
	}

}
