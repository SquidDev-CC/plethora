package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

public class LineLoop extends Polygon implements Scalable {
	private float scale = 1;

	public LineLoop(int id, int parent) {
		super(id, ObjectRegistry.LINE_LOOP_2D);
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
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		scale = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw2D(CanvasClient canvas) {
		if (points.size() < 2) return;

		setupFlat();
		GL11.glLineWidth(scale);

		GL11.glBegin(GL11.GL_LINE_LOOP);
		setupColour();
		for (Point2D point : points) GL11.glVertex3f(point.x, point.y, 0);
		GL11.glEnd();

		GL11.glLineWidth(1);
	}
}
