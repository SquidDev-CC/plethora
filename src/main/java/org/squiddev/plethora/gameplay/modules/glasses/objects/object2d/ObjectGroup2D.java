package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.renderer.GlStateManager;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;

public class ObjectGroup2D extends BaseObject implements ObjectGroup.Group2D, Positionable2D {
	private Point2D position = new Point2D();

	public ObjectGroup2D(int id, int parent) {
		super(id, parent, ObjectRegistry.GROUP_2D);
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
	public void writeInitial(ByteBuf buf) {
		position.write(buf);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position.read(buf);
	}

	@Override
	public void draw2D(CanvasClient canvas) {
		IntSet children = canvas.getChildren(id());
		if (children == null) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, 0);

		for (IntIterator iterator = children.iterator(); iterator.hasNext(); ) {
			int id = iterator.nextInt();
			BaseObject object = canvas.getObject(id);
			if (object != null) object.draw2D(canvas);
		}

		GlStateManager.popMatrix();
	}
}
