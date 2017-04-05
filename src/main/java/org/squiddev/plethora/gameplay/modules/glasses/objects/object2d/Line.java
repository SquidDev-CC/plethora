package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.LINE_2D;

public class Line extends BaseObject implements Colourable, Scalable {
	private int colour = DEFAULT_COLOUR;
	private float startX;
	private float startY;
	private float endX;
	private float endY;
	private float thickness = 1;

	public Line(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return LINE_2D;
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
	public float getScale() {
		return thickness;
	}

	@Override
	public void setScale(float scale) {
		if (this.thickness != scale) {
			this.thickness = scale;
			setDirty();
		}
	}

	public float getStartX() {
		return startX;
	}

	public float getStartY() {
		return startY;
	}

	public float getEndX() {
		return endX;
	}

	public float getEndY() {
		return endY;
	}

	public void setStart(float x, float y) {
		if (this.startX != x || this.startY != y) {
			this.startX = x;
			this.startY = y;
			setDirty();
		}
	}

	public void setEnd(float x, float y) {
		if (this.endX != x || this.endY != y) {
			this.endX = x;
			this.endY = y;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeFloat(startX);
		buf.writeFloat(startY);
		buf.writeFloat(endX);
		buf.writeFloat(endY);
		buf.writeFloat(thickness);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		startX = buf.readFloat();
		startY = buf.readFloat();
		endX = buf.readFloat();
		endY = buf.readFloat();
		thickness = buf.readFloat();
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		GL11.glLineWidth(thickness);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(startX, startY, 0);
		GL11.glVertex3f(endX, endY, 0);
		GL11.glEnd();
		GL11.glLineWidth(1);
	}
}
