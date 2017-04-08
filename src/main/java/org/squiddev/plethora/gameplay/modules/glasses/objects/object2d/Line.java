package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.LINE_2D;

public class Line extends BaseObject implements Colourable, Scalable, MultiPoint2D {
	private int colour = DEFAULT_COLOUR;
	private Point2D start = new Point2D();
	private Point2D end = new Point2D();
	private float thickness = 1;

	public Line(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return LINE_2D;
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
	public Point2D getPoint(int idx) {
		return idx == 0 ? start : end;
	}

	@Override
	public void setVertex(int idx, Point2D point) {
		if (idx == 0) {
			if (!Objects.equal(start, point)) {
				start = point;
				setDirty();
			}
		} else {
			if (!Objects.equal(end, point)) {
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
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		start.write(buf);
		end.write(buf);
		buf.writeFloat(thickness);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		start.read(buf);
		end.read(buf);
		thickness = buf.readFloat();
	}

	@Override
	public void draw3D(Entity viewEntity) {
	}

	@Override
	public void draw2D() {
		GL11.glLineWidth(thickness);
		GL11.glBegin(GL11.GL_LINES);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(start.x, start.y, 0);
		GL11.glVertex3f(end.x, end.y, 0);
		GL11.glEnd();
		GL11.glLineWidth(1);
	}
}
