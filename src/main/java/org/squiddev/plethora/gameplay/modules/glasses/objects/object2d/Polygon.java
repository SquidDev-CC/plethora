package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import java.util.ArrayList;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.POLYGON_2D;

public class Polygon extends BaseObject implements MultiPoint2D, MultiPointResizable2D, Colourable {
	protected int colour = DEFAULT_COLOUR;
	protected final ArrayList<Point2D> points = new ArrayList<Point2D>();

	public Polygon(int id) {
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
		return points.get(idx).x;
	}

	@Override
	public float getY(int idx) {
		return points.get(idx).y;
	}

	@Override
	public void setVertex(int idx, float x, float y) {
		Point2D point = points.get(idx);
		if (point.x != x || point.y != y) {
			points.set(idx, new Point2D(x, y));
		}
	}

	@Override
	public int getVerticies() {
		return points.size();
	}

	@Override
	public void removePoint(int idx) {
		points.remove(idx);
		setDirty();
	}

	@Override
	public void addPoint(int idx, float x, float y) {
		if (idx == points.size()) {
			points.add(new Point2D(x, y));
		} else {
			points.add(idx, new Point2D(x, y));
		}

		setDirty();
	}

	@Override
	public byte getType() {
		return POLYGON_2D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeByte(points.size());

		for (Point2D point : points) point.write(buf);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		int count = buf.readUnsignedByte();
		points.ensureCapacity(count);

		for (int i = points.size() - 1; i >= count; i--) points.remove(i);

		for (int i = 0; i < count; i++) {
			Point2D point;

			if (i < points.size()) {
				point = points.get(i);
			} else {
				points.add(point = new Point2D());
			}

			point.read(buf);
		}
	}

	@Override
	public void draw3D(Tessellator tessellator) {
	}

	@Override
	public void draw2D() {
		if (points.size() < 3) return;

		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		for (Point2D point : points) GL11.glVertex3f(point.x, point.y, 0);
		GL11.glEnd();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}
}
