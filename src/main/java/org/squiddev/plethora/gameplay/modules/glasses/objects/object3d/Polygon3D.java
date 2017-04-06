package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import java.util.ArrayList;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.POLYGON_3D;

public class Polygon3D extends BaseObject implements MultiPoint3D, MultiPointResizable3D, Colourable {
	protected int colour = DEFAULT_COLOUR;
	protected final ArrayList<Point3D> points = new ArrayList<Point3D>();

	public Polygon3D(int id) {
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
	public Point3D getPoint(int idx) {
		return points.get(idx);
	}

	@Override
	public void setVertex(int idx, Point3D point) {
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
	public void addPoint(int idx, Point3D point) {
		if (idx == points.size()) {
			points.add(point);
		} else {
			points.add(idx, point);
		}

		setDirty();
	}

	@Override
	public byte getType() {
		return POLYGON_3D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		buf.writeByte(points.size());

		for (Point3D point : points) point.write(buf);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		int count = buf.readUnsignedByte();
		points.ensureCapacity(count);

		for (int i = points.size() - 1; i >= count; i--) points.remove(i);

		for (int i = 0; i < count; i++) {
			Point3D point;

			if (i < points.size()) {
				point = points.get(i);
			} else {
				points.add(point = new Point3D());
			}

			point.read(buf);
		}
	}

	@Override
	public void draw3D(Entity viewEntity) {
		if (points.size() < 3) return;

		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		for (Point3D point : points) GL11.glVertex3f(point.x, point.y, point.z);
		GL11.glEnd();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	@Override
	public void draw2D() {
	}
}
