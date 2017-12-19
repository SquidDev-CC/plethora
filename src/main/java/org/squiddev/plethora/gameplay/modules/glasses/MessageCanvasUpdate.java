package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;

import java.util.ArrayList;
import java.util.List;

public class MessageCanvasUpdate implements IMessage {
	private int canvasId;
	private List<BaseObject> changed;
	private List<BaseObject> added;
	private int[] removed;

	public MessageCanvasUpdate(int canvasId, List<BaseObject> changed, List<BaseObject> added, int[] removed) {
		this.canvasId = canvasId;
		this.changed = changed;
		this.added = added;
		this.removed = removed;
	}

	public MessageCanvasUpdate() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canvasId = buf.readInt();

		int changedLength = buf.readInt();
		changed = new ArrayList<BaseObject>(changedLength);
		for (int i = 0; i < changedLength; i++) {
			changed.add(ObjectRegistry.read(buf));
		}

		int addedLength = buf.readInt();
		added = new ArrayList<BaseObject>(addedLength);
		for (int i = 0; i < addedLength; i++) {
			added.add(ObjectRegistry.read(buf));
		}

		int removedLength = buf.readInt();
		removed = new int[removedLength];
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

		buf.writeInt(added.size());
		for (BaseObject object : added) {
			ObjectRegistry.write(buf, object);
		}

		buf.writeInt(removed.length);
		for (int id : removed) {
			buf.writeInt(id);
		}
	}

	public static class Handler implements IMessageHandler<MessageCanvasUpdate, IMessage> {
		@Override
		public IMessage onMessage(MessageCanvasUpdate message, MessageContext context) {
			CanvasClient canvas = CanvasHandler.getClient(message.canvasId);
			if (canvas == null) return null;

			synchronized (canvas.objects) {
				for (BaseObject obj : message.changed) canvas.objects.put(obj.id, obj);
				for (BaseObject obj : message.added) canvas.objects.put(obj.id, obj);
				for (int id : message.removed) canvas.objects.remove(id);
			}

			return null;
		}
	}
}
