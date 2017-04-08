package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TRIANGLE_2D;

public class Triangle extends BaseObject implements MultiPoint2D, Colourable {
	private Point2D[] points = new Point2D[3];

	private int colour = DEFAULT_COLOUR;

	public Triangle(int id) {
		super(id);
		for (int i = 0; i < points.length; i++) points[i] = new Point2D();
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
		return points[idx];
	}

	@Override
	public void setVertex(int idx, Point2D point) {
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
		return TRIANGLE_2D;
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		for (Point2D point : points) {
			point.write(buf);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		for (Point2D point : points) {
			point.read(buf);
		}
	}

	@Override
	public void draw3D(Entity viewEntity) {
	}

	@Override
	public void draw2D() {
		GlStateManager.disableCull();

		GL11.glBegin(GL11.GL_TRIANGLES);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(points[0].x, points[0].y, 0);
		GL11.glVertex3f(points[1].x, points[1].y, 0);
		GL11.glVertex3f(points[2].x, points[2].y, 0);
		GL11.glEnd();

		GlStateManager.enableCull();
	}
}
