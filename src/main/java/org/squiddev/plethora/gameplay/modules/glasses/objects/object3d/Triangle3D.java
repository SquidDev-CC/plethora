package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TRIANGLE_3D;

public class Triangle3D extends BaseObject implements MultiPoint3D, Colourable {
	private Point3D[] points = new Point3D[3];

	private int colour = DEFAULT_COLOUR;

	public Triangle3D(int id) {
		super(id);
		for (int i = 0; i < points.length; i++) points[i] = new Point3D();
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
		return points[idx];
	}

	@Override
	public void setVertex(int idx, Point3D point) {
		if (!Objects.equal(points[idx], point)) {
			points[idx] = point;
			setDirty();
		}
	}

	@Override
	public int getVertices() {
		return 3;
	}

	@Override
	public byte getType() {
		return TRIANGLE_3D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		for (Point3D point : points) {
			point.write(buf);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		for (Point3D point : points) {
			point.read(buf);
		}
	}

	@Override
	public void draw3D(Entity viewEntity) {
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(points[0].x, points[0].y, points[0].z);
		GL11.glVertex3f(points[1].x, points[1].y, points[1].z);
		GL11.glVertex3f(points[2].x, points[2].y, points[2].z);
		GL11.glEnd();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	@Override
	public void draw2D() {
	}
}
