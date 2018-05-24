package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;

import java.util.ArrayList;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.POLYGON_2D;

public class Polygon extends ColourableObject implements MultiPoint2D, MultiPointResizable2D {
	protected final ArrayList<Point2D> points = new ArrayList<Point2D>();

	public Polygon(int id) {
		super(id);
	}

	@Override
	public Point2D getPoint(int idx) {
		return points.get(idx);
	}

	@Override
	public void setVertex(int idx, Point2D point) {
		if (!Objects.equal(points.get(idx), point)) {
			points.set(idx, point);
			setDirty();
		}
	}

	@Override
	public int getVertices() {
		return points.size();
	}

	@Override
	public void removePoint(int idx) {
		points.remove(idx);
		setDirty();
	}

	@Override
	public void addPoint(int idx, Point2D point) {
		if (idx == points.size()) {
			points.add(point);
		} else {
			points.add(idx, point);
		}

		setDirty();
	}

	@Override
	public byte getType() {
		return POLYGON_2D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		super.writeInital(buf);
		buf.writeByte(points.size());

		for (Point2D point : points) point.write(buf);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
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
	@SideOnly(Side.CLIENT)
	public void draw2D() {
		if (points.size() < 3) return;

		setupFlat();

		int size = points.size();
		Point2D a = points.get(0);

		GL11.glBegin(GL11.GL_TRIANGLES);
		setupColour();
		for (int i = 1; i < size - 1; i++) {
			Point2D b = points.get(i), c = points.get(i + 1);
			GL11.glVertex3f(a.x, a.y, 0);
			GL11.glVertex3f(b.x, b.y, 0);
			GL11.glVertex3f(c.x, c.y, 0);
		}
		GL11.glEnd();
	}
}
