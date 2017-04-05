package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.DOT_2D;

public class Dot extends BaseObject implements Positionable2D, Colourable, Scalable {
	private int colour = DEFAULT_COLOUR;
	private float x;
	private float y;
	private float scale = 1;

	public Dot(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return DOT_2D;
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
		return scale;
	}

	@Override
	public void setScale(float scale) {
		if (this.scale != scale) {
			this.scale = scale;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeFloat(x);
		buf.writeFloat(y);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		x = buf.readFloat();
		y = buf.readFloat();
		scale = buf.readFloat();
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		GL11.glPointSize(scale);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(x, y, 0);
		GL11.glEnd();
		GL11.glPointSize(1);
	}
}
