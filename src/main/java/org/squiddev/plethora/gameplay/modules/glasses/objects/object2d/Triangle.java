package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TRIANGLE_2D;

public class Triangle extends BaseObject implements MultiPoint2D, Colourable {
	private float[] points = new float[6];

	private int colour = DEFAULT_COLOUR;

	public Triangle(int id) {
		super(id);
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
	public float getX(int idx) {
		return points[idx * 2];
	}

	@Override
	public float getY(int idx) {
		return points[idx * 2 + 1];
	}

	@Override
	public void setVertex(int idx, float x, float y) {
		if (points[idx * 2] != x | points[idx * 2 + 1] != y) {
			points[idx * 2] = x;
			points[idx * 2 + 1] = y;
			setDirty();
		}
	}

	@Override
	public int getVerticies() {
		return 6;
	}

	@Override
	public byte getType() {
		return TRIANGLE_2D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		for (float point : points) {
			buf.writeFloat(point);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		for (int i = 0; i < points.length; i++) {
			points[i] = buf.readFloat();
		}
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(points[0], points[1], 0);
		GL11.glVertex3f(points[2], points[3], 0);
		GL11.glVertex3f(points[4], points[5], 0);
		GL11.glEnd();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}
}
