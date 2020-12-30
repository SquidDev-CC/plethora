package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

public class MessageCanvasRemove implements BasicMessage {
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

	@Override
	public void onMessage(MessageContext context) {
		CanvasClient canvas = CanvasHandler.getClient(canvasId);
		if (canvas == null) return;

		CanvasHandler.removeClient(canvas);
	}
}
