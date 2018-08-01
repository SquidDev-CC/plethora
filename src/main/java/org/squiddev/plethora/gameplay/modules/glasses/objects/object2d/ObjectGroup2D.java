package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.renderer.GlStateManager;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

public class ObjectGroup2D extends BaseObject implements ObjectGroup.Group2D, Positionable2D {
	private Vec2d position = Vec2d.ZERO;

	public ObjectGroup2D(int id, int parent) {
		super(id, parent, ObjectRegistry.GROUP_2D);
	}

	@Override
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(Vec2d position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec2d(buf, position);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec2d(buf);
	}

	@Override
	public void draw(CanvasClient canvas) {
		IntSet children = canvas.getChildren(id());
		if (children == null) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, 0);

		canvas.drawChildren(children.iterator());

		GlStateManager.popMatrix();
	}
}
