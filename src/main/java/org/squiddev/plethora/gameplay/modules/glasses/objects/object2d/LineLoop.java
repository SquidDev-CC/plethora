package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.Vec2d;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;

public class LineLoop extends Polygon implements Scalable {
	private float scale = 1;

	public LineLoop(int id, int parent) {
		super(id, parent, ObjectRegistry.LINE_LOOP_2D);
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
		buf.writeFloat(scale);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		scale = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		if (points.size() < 2) return;

		setupFlat();
		GlStateManager.disableCull();
		GlStateManager.color(1, 1, 1);
		GlStateManager.glLineWidth(scale);

		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

		for (Vec2d point : points) buffer.pos(point.x, point.y, 0).color(red, green, blue, alpha).endVertex();

		tessellator.draw();

		GlStateManager.glLineWidth(1);
	}
}
