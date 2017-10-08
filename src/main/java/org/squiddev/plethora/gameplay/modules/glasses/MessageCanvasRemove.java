package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCanvasRemove implements IMessage {
	private int canvasId;

	public MessageCanvasRemove(int canvasId) {
		this.canvasId = canvasId;
	}

	public MessageCanvasRemove() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canvasId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(canvasId);
	}

	public static class Handler implements IMessageHandler<MessageCanvasRemove, IMessage> {
		@Override
		public IMessage onMessage(MessageCanvasRemove message, MessageContext context) {
			CanvasClient canvas = CanvasHandler.getClient(message.canvasId);
			if (canvas == null) return null;

			CanvasHandler.removeClient(canvas);

			return null;
		}
	}
}
