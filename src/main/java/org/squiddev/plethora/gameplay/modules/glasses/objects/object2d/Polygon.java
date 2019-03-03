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
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class Polygon extends ColourableObject implements MultiPoint2D, MultiPointResizable2D {
	protected final ArrayList<Vec2d> points = new ArrayList<>();

	protected Polygon(int id, int parent, byte type) {
		super(id, parent, type);
	}

	public Polygon(int id, int parent) {
		super(id, parent, ObjectRegistry.POLYGON_2D);
	}

	@Nonnull
	@Override
	public Vec2d getPoint(int idx) {
		return points.get(idx);
	}

	@Override
	public void setVertex(int idx, @Nonnull Vec2d point) {
		if (!Objects.equal(points.get(idx), point)) {
			points.set(idx, point);
			setDirty();
		}
	}

	@Override
	public int getVertices() {
		return points.size();
	}

	@Override
	public void removePoint(int idx) {
		points.remove(idx);
		setDirty();
	}

	@Override
	public void addPoint(int idx, @Nonnull Vec2d point) {
		if (idx == points.size()) {
			points.add(point);
		} else {
			points.add(idx, point);
		}

		setDirty();
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		buf.writeByte(points.size());

		for (Vec2d point : points) ByteBufUtils.writeVec2d(buf, point);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		int count = buf.readUnsignedByte();
		points.ensureCapacity(count);

		if (points.size() > count) points.subList(count, points.size()).clear();

		for (int i = 0; i < count; i++) {
			Vec2d point = ByteBufUtils.readVec2d(buf);
			if (i < points.size()) {
				points.set(i, point);
			} else {
				points.add(point);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		if (points.size() < 3) return;

		setupFlat();

		int size = points.size();
		Vec2d a = points.get(0);

		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		for (int i = 1; i < size - 1; i++) {
			Vec2d b = points.get(i), c = points.get(i + 1);
			buffer.pos(a.x, a.y, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(b.x, b.y, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(c.x, c.y, 0).color(red, green, blue, alpha).endVertex();
		}

		tessellator.draw();
	}
}
