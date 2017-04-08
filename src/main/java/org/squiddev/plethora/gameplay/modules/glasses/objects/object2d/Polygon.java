package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
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
	public void draw3D(Entity viewEntity) {
	}

	@Override
	public void draw2D() {
		if (points.size() < 3) return;

		GlStateManager.disableCull();

		GL11.glBegin(GL11.GL_POLYGON);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		for (Point2D point : points) GL11.glVertex3f(point.x, point.y, 0);
		GL11.glEnd();

		GlStateManager.enableCull();
	}
}
