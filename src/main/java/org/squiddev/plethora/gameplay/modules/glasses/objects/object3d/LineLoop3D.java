package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.LINE_LOOP_3D;

public class LineLoop3D extends Polygon3D implements Scalable {
	private float scale = 1;

	public LineLoop3D(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return LINE_LOOP_3D;
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
	public void draw3D(Entity viewEntity) {
		if (points.size() < 2) return;

		GL11.glLineWidth(scale);

		GL11.glBegin(GL11.GL_LINE_LOOP);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		for (Point3D point : points) GL11.glVertex3f(point.x, point.y, point.z);
		GL11.glEnd();

		GL11.glLineWidth(1);
	}
}
