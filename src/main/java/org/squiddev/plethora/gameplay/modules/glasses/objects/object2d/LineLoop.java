package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.LINE_LOOP_2D;

public class LineLoop extends Polygon implements Scalable {
	private float scale = 1;

	public LineLoop(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return LINE_LOOP_2D;
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
	public void writeInital(ByteBuf buf) {
		super.writeInital(buf);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		scale = buf.readFloat();
	}

	@Override
	public void draw2D() {
		if (points.size() < 2) return;

		GL11.glLineWidth(scale);

		GL11.glBegin(GL11.GL_LINE_LOOP);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		for (Point2D point : points) GL11.glVertex3f(point.x, point.y, 0);
		GL11.glEnd();

		GL11.glLineWidth(1);
	}
}
