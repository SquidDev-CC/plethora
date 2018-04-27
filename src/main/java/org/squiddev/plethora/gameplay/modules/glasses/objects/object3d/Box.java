package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.GeometryMasks;
import org.squiddev.plethora.utils.ShapeTesellator;

public class Box extends ColourableObject implements Positionable3D, Sizeable3D, DepthTestable {
	private Vec3d position;
	private double width;
	private double height;
	private double depth;

	private boolean depthTest;

	public Box(int id, int parent) {
		super(id, parent, ObjectRegistry.BOX_3D);
	}

	@Override
	public void setDepthTestingEnabled(boolean testingEnabled) {
		if (this.depthTest != testingEnabled) {
			this.depthTest = testingEnabled;
			setDirty();
		}
	}

	@Override
	public boolean isDepthTestingEnabled() {
		return depthTest;
	}

	@Override
	public Vec3d getPosition() {
		return position;
	}

	@Override
	public void setPosition(Vec3d position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
			setDirty();
		}
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public double getDepth() {
		return depth;
	}

	@Override
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
		width = buf.readDouble();
		height = buf.readDouble();
		depth = buf.readDouble();
		depthTest = buf.readBoolean();
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);

		ByteBufUtils.writeVec3d(buf, position);
		buf.writeDouble(width);
		buf.writeDouble(height);
		buf.writeDouble(depth);
		buf.writeBoolean(depthTest);
	}

	@Override
	public void draw(CanvasClient canvas) {
		if (depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);

		// Add a stupidly small number to the coords to prevent colliding faces looking terrible
		double offset = 0.003D;
		ShapeTesellator.prepare(GL11.GL_QUADS);
		ShapeTesellator.drawBox(
			position.x + offset, position.y + offset, position.z + offset,
			width - offset * 2, height - offset * 2, depth - offset * 2,
			Integer.rotateRight(getColour(), 8),
			GeometryMasks.Quad.ALL
		);
		ShapeTesellator.release();

		if (depthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
}
