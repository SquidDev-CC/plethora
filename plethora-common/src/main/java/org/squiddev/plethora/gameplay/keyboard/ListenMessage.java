package org.squiddev.plethora.gameplay.keyboard;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

public class ListenMessage implements BasicMessage {
	private boolean listening;

	public ListenMessage() {
	}

	public ListenMessage(boolean listening) {
		this.listening = listening;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		listening = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(listening);
	}

	@Override
	public void onMessage(MessageContext context) {
		ClientKeyListener.listening = listening;
	}
}
