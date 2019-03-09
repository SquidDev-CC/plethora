package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;

public class Box extends ColourableObject implements Positionable3D, DepthTestable {
	private Vec3d position;
	private double width;
	private double height;
	private double depth;

	private boolean depthTest = true;

	public Box(int id, int parent) {
		super(id, parent, ObjectRegistry.BOX_3D);
	}

	@Override
	public boolean hasDepthTest() {
		return depthTest;
	}

	@Override
	public void setDepthTest(boolean depthTest) {
		if (this.depthTest != depthTest) {
			this.depthTest = depthTest;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec3d position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
			setDirty();
		}
	}

	public void setSize(double width, double height, double depth) {
		if (this.width != width || this.height != height || this.depth != depth) {
			this.width = width;
			this.height = height;
			this.depth = depth;
			setDirty();
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position = ByteBufUtils.readVec3d(buf);
		width = buf.readFloat();
		height = buf.readFloat();
		depth = buf.readFloat();
		depthTest = buf.readBoolean();
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec3d(buf, position);
		buf.writeFloat((float) width);
		buf.writeFloat((float) height);
		buf.writeFloat((float) depth);
		buf.writeBoolean(depthTest);
	}

	@Override
	public void draw(CanvasClient canvas) {
		setupFlat();
		if (depthTest) {
			GlStateManager.enableDepth();
		} else {
			GlStateManager.disableDepth();
		}

		double minX = position.x, minY = position.y, minZ = position.z;
		double maxX = minX + width, maxY = minY + height, maxZ = minZ + depth;
		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

		//up
		buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();

		//north
		buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

		//south
		buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

		//east
		buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();

		//west
		buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

		tessellator.draw();
	}

	@PlethoraMethod(doc = "function():number, number, number -- Get the size of this box.", worldThread = false)
	public static MethodResult getSize(@FromTarget Box rect) {
		return MethodResult.result(rect.width, rect.height, rect.depth);
	}

	@PlethoraMethod(doc = "function(width:number, height:number, depth:number) -- Set the size of this box.", worldThread = false)
	public static void setSize(@FromTarget Box rect, float width, float height, float depth) {
		rect.setSize(width, height, depth);
	}
}
