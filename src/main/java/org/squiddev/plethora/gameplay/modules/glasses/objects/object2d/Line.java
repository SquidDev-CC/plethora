package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLSync;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

public class Line extends ColourableObject implements Scalable, MultiPoint2D {
	private Vec2d start = Vec2d.ZERO;
	private Vec2d end = Vec2d.ZERO;
	private float thickness = 1;

	public Line(int id, int parent) {
		super(id, parent, ObjectRegistry.LINE_2D);
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

	@Nonnull
	@Override
	public Vec2d getPoint(int idx) {
		return idx == 0 ? start : end;
	}

	@Override
	public void setVertex(int idx, @Nonnull Vec2d point) {
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
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec2d(buf, start);
		ByteBufUtils.writeVec2d(buf, end);
		buf.writeFloat(thickness);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		start = ByteBufUtils.readVec2d(buf);
		end = ByteBufUtils.readVec2d(buf);
		thickness = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		setupFlat();
		GlStateManager.disableCull();
		GlStateManager.color(1, 1, 1);
		GL11.glLineWidth(thickness);

		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos((float) start.x, (float) start.y, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos((float) end.x, (float) end.y, 0).color(red, green, blue, alpha).endVertex();

		tessellator.draw();

		GL11.glLineWidth(1);
	}
}
