package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageCanvasUpdate implements BasicMessage {
	private int canvasId;
	private List<BaseObject> changed;
	private int[] removed;

	public MessageCanvasUpdate(int canvasId, List<BaseObject> changed, int[] removed) {
		this.canvasId = canvasId;
		this.changed = changed;
		this.removed = removed;
	}

	public MessageCanvasUpdate() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canvasId = buf.readInt();

		int changedLength = buf.readInt();
		List<BaseObject> changed = this.changed = new ArrayList<>(changedLength);
		for (int i = 0; i < changedLength; i++) {
			changed.add(ObjectRegistry.read(buf));
		}

		// We sort on ID in order to guarantee parents are loaded before their children
		changed.sort(BaseObject.SORTING_ORDER);

		int removedLength = buf.readInt();
		int[] removed = this.removed = new int[removedLength];
		for (int i = 0; i < removedLength; i++) {
			removed[i] = buf.readInt();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(canvasId);

		buf.writeInt(changed.size());
		for (BaseObject object : changed) {
			ObjectRegistry.write(buf, object);
		}

		buf.writeInt(removed.length);
		for (int id : removed) {
			buf.writeInt(id);
		}
	}

	@Override
	public void onMessage(MessageContext context) {
		CanvasClient canvas = CanvasHandler.getClient(canvasId);
		if (canvas == null) return;

		synchronized (canvas) {
			for (BaseObject obj : changed) canvas.updateObject(obj);
			for (int id : removed) canvas.remove(id);
		}
	}
}
