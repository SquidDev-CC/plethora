package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ObjectFrame extends BaseObject implements ObjectGroup.Group2D, Positionable3D, Rotatable3D {
	private static final float SCALE = 1 / 16.0f;

	private Vec3d position = Vec3d.ZERO;
	private Vec3d rotation = null;

	public ObjectFrame(int id, int parent) {
		super(id, parent, ObjectRegistry.FRAME_3D);
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec3d position) {
		if (!this.position.equals(position)) {
			this.position = position;
			setDirty();
		}
	}

	@Nullable
	@Override
	public Vec3d getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(@Nullable Vec3d rotation) {
		if (!Objects.equals(this.rotation, rotation)) {
			this.rotation = rotation;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec3d(buf, position);
		if (rotation == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			ByteBufUtils.writeVec3d(buf, rotation);
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec3d(buf);
		rotation = buf.readBoolean() ? ByteBufUtils.readVec3d(buf) : null;
	}

	@Override
	public void draw(CanvasClient canvas) {
		IntSet children = canvas.getChildren(id());
		if (children == null) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		RenderManager renderManager = minecraft.getRenderManager();

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, position.z);
		GlStateManager.scale(SCALE, -SCALE, SCALE);
		if (rotation == null) {
			GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
			GlStateManager.rotate(renderManager.playerViewX, 1, 0, 0);
		} else {
			GlStateManager.rotate((float) rotation.x, 1, 0, 0);
			GlStateManager.rotate((float) rotation.y, 0, 1, 0);
			GlStateManager.rotate((float) rotation.z, 0, 0, 1);
		}

		canvas.drawChildren(children.iterator());

		GlStateManager.popMatrix();
	}
}
