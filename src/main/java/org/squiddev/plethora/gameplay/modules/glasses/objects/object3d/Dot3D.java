package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.DOT_3D;

public class Dot3D extends BaseObject implements Positionable3D, Colourable, Scalable {
	private int colour = DEFAULT_COLOUR;
	private Point3D position = new Point3D();
	private float scale = 1;

	public Dot3D(int id) {
		super(id);
	}

	@Override
	public byte getType() {
		return DOT_3D;
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
	public Point3D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point3D position) {
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
		GL11.glPointSize(scale);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glColor4f(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
		GL11.glVertex3f(position.x, position.y, position.z);
		GL11.glEnd();
		GL11.glPointSize(1);
	}

	@Override
	public void draw2D() {
	}
}
