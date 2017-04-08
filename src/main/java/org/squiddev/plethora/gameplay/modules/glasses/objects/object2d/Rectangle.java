package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.RECTANGLE_2D;

public class Rectangle extends BaseObject implements Positionable2D, Colourable {
	private int colour = DEFAULT_COLOUR;
	private Point2D position = new Point2D();
	private float width;
	private float height;

	public Rectangle(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return RECTANGLE_2D;
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

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setSize(float width, float height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
		position.write(buf);
		buf.writeFloat(width);
		buf.writeFloat(height);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
		position.read(buf);
		width = buf.readFloat();
		height = buf.readFloat();
	}

	@Override
	public void draw3D(Entity viewEntity) {
	}

	@Override
	public void draw2D() {
		float x = position.x, y = position.y;
		GL11.glBegin(GL11.GL_QUADS);
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(x, y, 0);
		GL11.glVertex3f(x, y + height, 0);
		GL11.glVertex3f(x + width, y + height, 0);
		GL11.glVertex3f(x + width, y + 0, 0);
		GL11.glEnd();
	}
}
