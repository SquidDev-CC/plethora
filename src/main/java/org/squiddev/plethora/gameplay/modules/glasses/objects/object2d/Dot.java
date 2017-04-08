package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.DOT_2D;

public class Dot extends BaseObject implements Positionable2D, Colourable, Scalable {
	private int colour = DEFAULT_COLOUR;
	private Point2D position = new Point2D();
	private float scale = 1;

	public Dot(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return DOT_2D;
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
	public Point2D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point2D position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
			setDirty();
		}
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
		buf.writeInt(colour);
		position.write(buf);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		position.read(buf);
		scale = buf.readFloat();
	}

	@Override
	public void draw3D(Entity viewEntity) {
	}

	@Override
	public void draw2D() {
		GL11.glPointSize(scale);
		GL11.glBegin(GL11.GL_POINTS);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(position.x, position.y, 0);
		GL11.glEnd();
		GL11.glPointSize(1);
	}
}
