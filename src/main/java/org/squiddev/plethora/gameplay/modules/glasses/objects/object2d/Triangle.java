package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.TRIANGLE_2D;

public class Triangle extends ColourableObject implements MultiPoint2D {
	private Point2D[] points = new Point2D[3];

	public Triangle(int id) {
		super(id);
		for (int i = 0; i < points.length; i++) points[i] = new Point2D();
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
		super.writeInital(buf);
		for (Point2D point : points) {
			point.write(buf);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		for (Point2D point : points) {
			point.read(buf);
		}
	}

	@Override
	public void draw2D() {
		GlStateManager.disableCull();

		GL11.glBegin(GL11.GL_TRIANGLES);
		setupColour();
		GL11.glVertex3f(points[0].x, points[0].y, 0);
		GL11.glVertex3f(points[1].x, points[1].y, 0);
		GL11.glVertex3f(points[2].x, points[2].y, 0);
		GL11.glEnd();

		GlStateManager.enableCull();
	}
}
