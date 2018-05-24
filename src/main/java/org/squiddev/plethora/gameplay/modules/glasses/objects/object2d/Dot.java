package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import static org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry.DOT_2D;

public class Dot extends ColourableObject implements Positionable2D, Scalable {
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
		super.writeInital(buf);
		position.write(buf);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position.read(buf);
		scale = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw2D() {
		setupFlat();

		float x = position.x, y = position.y, delta = scale / 2;

		GL11.glBegin(GL11.GL_TRIANGLES);
		setupColour();
		GL11.glVertex3f(x - delta, y - delta, 0);
		GL11.glVertex3f(x - delta, y + delta, 0);
		GL11.glVertex3f(x + delta, y + delta, 0);

		GL11.glVertex3f(x - delta, y - delta, 0);
		GL11.glVertex3f(x + delta, y + delta, 0);
		GL11.glVertex3f(x + delta, y - delta, 0);
		GL11.glEnd();
	}
}
