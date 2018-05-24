package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.RECTANGLE_2D;

public class Rectangle extends ColourableObject implements Positionable2D {
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
		super.writeInital(buf);
		position.write(buf);
		buf.writeFloat(width);
		buf.writeFloat(height);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position.read(buf);
		width = buf.readFloat();
		height = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw2D() {
		setupFlat();

		float x = position.x, y = position.y;
		GL11.glBegin(GL11.GL_TRIANGLES);
		setupColour();
		GL11.glVertex3f(x, y, 0);
		GL11.glVertex3f(x, y + height, 0);
		GL11.glVertex3f(x + width, y + height, 0);

		GL11.glVertex3f(x, y, 0);
		GL11.glVertex3f(x + width, y + height, 0);
		GL11.glVertex3f(x + width, y + 0, 0);
		GL11.glEnd();
	}
}
