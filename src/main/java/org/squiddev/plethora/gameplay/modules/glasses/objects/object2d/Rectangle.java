package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Positionable2D;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.RECTANGLE_2D;

public class Rectangle extends BaseObject implements Positionable2D, Colourable {
	private int colour;
	private float x;
	private float y;

	private float width;
	private float height;

	public Rectangle(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return RECTANGLE_2D;
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

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setSize(float width, float height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeFloat(x);
		buf.writeFloat(y);
		buf.writeFloat(width);
		buf.writeFloat(height);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		x = buf.readFloat();
		y = buf.readFloat();
		width = buf.readFloat();
		height = buf.readFloat();
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
//		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
//		GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);
		GL11.glVertex3f(x, y, 0);
		GL11.glVertex3f(x, y + width, 0);
		GL11.glVertex3f(x + height, y + width, 0);
		GL11.glVertex3f(x + height, y + 0, 0);
		GL11.glEnd();
	}
}
