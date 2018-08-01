package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

public class Triangle extends ColourableObject implements MultiPoint2D {
	private Vec2d[] points = new Vec2d[3];

	public Triangle(int id, int parent) {
		super(id, parent, ObjectRegistry.TRIANGLE_2D);
		for (int i = 0; i < points.length; i++) points[i] = Vec2d.ZERO;
	}

	@Override
	public Vec2d getPoint(int idx) {
		return points[idx];
	}

	@Override
	public void setVertex(int idx, Vec2d point) {
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
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		for (Vec2d point : points) ByteBufUtils.writeVec2d(buf, point);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		for (int i = 0; i < points.length; i++) points[i] = ByteBufUtils.readVec2d(buf);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		GlStateManager.disableCull();

		GL11.glBegin(GL11.GL_TRIANGLES);
		setupColour();
		GL11.glVertex3d(points[0].x, points[0].y, 0);
		GL11.glVertex3d(points[1].x, points[1].y, 0);
		GL11.glVertex3d(points[2].x, points[2].y, 0);
		GL11.glEnd();

		GlStateManager.enableCull();
	}
}
