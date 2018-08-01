package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.ShapeTesellator;

import static org.lwjgl.opengl.GL11.GL_LINES;

/**
 * Created by 086 on 29/04/2018.
 *
 * @see org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.Line
 */
public class Line3D extends ColourableObject implements MultiPoint3D, Scalable {
	private Vec3d start;
	private Vec3d end;
	private float thickness = 1;

	public Line3D(int id, int parent) {
		super(id, parent, ObjectRegistry.LINE_3D);
	}

	@Override
	public float getScale() {
		return thickness;
	}

	@Override
	public void setScale(float scale) {
		if (this.thickness != scale) {
			this.thickness = scale;
			setDirty();
		}
	}

	@Override
	public Vec3d getPoint(int idx) {
		return idx == 0 ? start : end;
	}

	@Override
	public void setVertex(int idx, Vec3d point) {
		if (idx == 0) {
			if (!point.equals(start)) {
				start = point;
				setDirty();
			}
		} else {
			if (!point.equals(end)) {
				end = point;
				setDirty();
			}
		}
	}

	@Override
	public int getVertices() {
		return 2;
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		start = ByteBufUtils.readVec3d(buf);
		end = ByteBufUtils.readVec3d(buf);
		buf.writeFloat(thickness);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		ByteBufUtils.writeVec3d(buf, start);
		ByteBufUtils.writeVec3d(buf, end);
		thickness = buf.readFloat();
	}

	@Override
	public void draw(CanvasClient canvas) {
		int rgba = getColour();
		final int r = (rgba >>> 24) & 0xFF;
		final int g = (rgba >>> 16) & 0xFF;
		final int b = (rgba >>> 8) & 0xFF;
		final int a = rgba & 0xFF;

		GL11.glLineWidth(thickness);

		BufferBuilder bufferBuilder = ShapeTesellator.getBufferBuilder();
		ShapeTesellator.prepare(GL_LINES);
		bufferBuilder.pos(start.x, start.y, start.z).color(r, g, b, a).endVertex();
		bufferBuilder.pos(end.x, end.y, end.z).color(r, g, b, a).endVertex();
		ShapeTesellator.release();

		GL11.glLineWidth(1);
	}
}
