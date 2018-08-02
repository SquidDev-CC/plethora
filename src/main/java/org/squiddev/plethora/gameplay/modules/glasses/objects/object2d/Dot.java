package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class Dot extends ColourableObject implements Positionable2D, Scalable {
	private Vec2d position = Vec2d.ZERO;
	private float scale = 1;

	public Dot(int id, int parent) {
		super(id, parent, ObjectRegistry.DOT_2D);
	}

	@Nonnull
	@Override
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec2d position) {
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
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec2d(buf, position);
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position = ByteBufUtils.readVec2d(buf);
		scale = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		setupFlat();

		float x = (float) position.x, y = (float) position.y, delta = scale / 2;
		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(x - delta, y - delta, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x - delta, y + delta, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + delta, y + delta, 0).color(red, green, blue, alpha).endVertex();

		buffer.pos(x - delta, y - delta, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + delta, y + delta, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + delta, y - delta, 0).color(red, green, blue, alpha).endVertex();

		tessellator.draw();
	}
}
