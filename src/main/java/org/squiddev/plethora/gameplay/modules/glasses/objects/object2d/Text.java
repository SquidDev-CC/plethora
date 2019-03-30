package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Textable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class Text extends ColourableObject implements Positionable2D, Scalable, Textable {
	/**
	 * We use a two dimensional string array to indicate where tabs are.
	 * For example, {@code "Hello\tworld\nFoo\tBar"} would become
	 * {@code {{"Hello", "world"}, {"Foo", "Bar"}}}.
	 *
	 * This is used in the rendering to simulate tabs.
	 */
	private static final String[][] EMPTY_LINES = new String[][]{};

	/**
	 * A tab is 4 spaces and one space is 4 pixels wide -> 1 tab is 4*4 (16) pixels wide.
	 * Used during rendering
	 */
	private static final int TAB_WIDTH = 16;

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\r\n|\n|\r");

	private Vec2d position = Vec2d.ZERO;
	private float size = 1;
	private short lineHeight = 9;
	private boolean dropShadow = false;

	private String text = "";
	private String[][] lines = EMPTY_LINES;

	public Text(int id, int parent) {
		super(id, parent, ObjectRegistry.TEXT_2D);
	}

	@Nonnull
	@Override
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec2d position) {
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
		if (size != scale) {
			size = scale;
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
		if (this.dropShadow != dropShadow) {
			this.dropShadow = dropShadow;
			setDirty();
		}
	}

	@Override
	public boolean hasShadow() {
		return dropShadow;
	}

	@Override
	public void setLineHeight(short lineHeight) {
		if (this.lineHeight == lineHeight) return;
		this.lineHeight = lineHeight;
		setDirty();
	}

	@Override
	public short getLineHeight() {
		return lineHeight;
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec2d(buf, position);
		buf.writeFloat(size);
		buf.writeBoolean(dropShadow);
		buf.writeShort(lineHeight);
		ByteBufUtils.writeUTF8String(buf, text);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position = ByteBufUtils.readVec2d(buf);
		size = buf.readFloat();
		dropShadow = buf.readBoolean();
		lineHeight = buf.readShort();
		text = ByteBufUtils.readUTF8String(buf);
		lines = splitText(text);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		int colour = getColour();

		// If the alpha channel doesn't match a 0xFC, then the font renderer
		// will make it opaque. We also early exit here if we're transparent.
		int alpha = colour & 0xFF;
		if (alpha == 0) return;
		if ((alpha & 0xFC) == 0) colour |= 0x4;

		// TODO: Try to have some common method on BasicObject
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();

		FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, 0);
		GlStateManager.scale(size, size, 1);

		int y = 0;
		for (String[] fullLine : lines) {
			int x = 0;
			for (String tabSection : fullLine) {
				// We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
				x = fontrenderer.drawString(tabSection, x, y, Integer.rotateRight(colour, 8), dropShadow);

				// Round the X coordinate to the next tab stop.
				x = (x / TAB_WIDTH) * TAB_WIDTH + TAB_WIDTH;
			}

			y += lineHeight;
		}

		GlStateManager.popMatrix();
	}

	private static String[][] splitText(String text) {
		String[] lines = SPLIT_PATTERN.split(text);

		String[][] splitLines = new String[lines.length][];
		StringBuilder format = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			String[] tabs = splitLines[i] = lines[i].split("\t");

			for (int j = 0; j < tabs.length; j++) {
				String tab = tabs[j];
				format.append(tab);

				appendFormat(format, tabs[j] = format.toString());
			}
		}

		return splitLines;
	}

	private static void appendFormat(StringBuilder builder, String text) {
		builder.setLength(0);

		int l = text.length();
		int i = -1;
		while ((i = text.indexOf('\u00a7', i + 1)) != -1) {
			if (i < l - 1) {
				char c0 = text.charAt(i + 1);

				if (isFormatColor(c0)) {
					builder.setLength(0);
					builder.append('\u00a7').append(c0);
				} else if (isFormatSpecial(c0)) {
					builder.append('\u00a7').append(c0);
				}
			}
		}
	}

	private static boolean isFormatColor(char colorChar) {
		return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
	}

	/**
	 * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
	 */
	private static boolean isFormatSpecial(char formatChar) {
		return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
	}
}
