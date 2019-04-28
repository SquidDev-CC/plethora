package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;

public class Line3D extends ColourableObject implements Positionable3D, DepthTestable, Scalable {
	private Vec3d start;
	private Vec3d end;

	private float thickness = 1.0f;
	private boolean depthTest = true;

	public Line3D(int id, int parent) {
		super(id, parent, ObjectRegistry.LINE_3D);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		setupFlat();

		if (start != null && end != null) {
			if (depthTest) {
				GlStateManager.enableDepth();
			} else {
				GlStateManager.disableDepth();
			}

			GlStateManager.glLineWidth(thickness);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			buffer.pos(start.x, start.y, start.z).color(getRed(), getGreen(), getBlue(), getAlpha()).endVertex();
			buffer.pos(end.x, end.y, end.z).color(getRed(), getGreen(), getBlue(), getAlpha()).endVertex();

			tessellator.draw();

			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GlStateManager.glLineWidth(1.0f);
		}
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

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec3d(buf, start);
		ByteBufUtils.writeVec3d(buf, end);
		buf.writeFloat(thickness);
		buf.writeBoolean(depthTest);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		start = ByteBufUtils.readVec3d(buf);
		end = ByteBufUtils.readVec3d(buf);
		thickness = buf.readFloat();
		depthTest = buf.readBoolean();
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return start;
	}

	@Nonnull
	public Vec3d getEndPosition() {
		return end;
	}

	@Override
	public void setPosition(@Nonnull Vec3d position) {
		if (!Objects.equal(this.start, position)) {
			this.start = position;
			setDirty();
		}
	}
	
	public void setEndPosition(@Nonnull Vec3d position) {
		if (!Objects.equal(this.end, position)) {
			this.end = position;
			setDirty();
		}
	}

	@PlethoraMethod(doc = "function():number, number, number -- Get the end position of this line.", worldThread = false)
	public static MethodResult getEndPosition(@FromTarget Line3D line) {
		return MethodResult.result(line.end.x, line.end.y, line.end.z);
	}

	@PlethoraMethod(doc = "function(endX:number, endY:number, endZ:number) -- Set the end position of this line.", worldThread = false)
	public static void setEndPosition(@FromTarget Line3D line, float endX, float endY, float endZ) {
		line.setEndPosition(new Vec3d(endX, endY, endZ));
	}

	@Override
	public float getScale() {
		return thickness;
	}

	@Override
	public void setScale(float scale) {
		if (thickness != scale) {
			// Large line widths can behave weirdly on some hardware (or not work at all), so let's keep it sane.
			this.thickness = MathHelper.clamp(scale, 1.0f, 5.0f);
			setDirty();
		}
	}
}
