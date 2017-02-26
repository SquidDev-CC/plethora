package org.squiddev.plethora.gameplay.keyboard;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ListenMessage implements IMessage {
	public boolean listen;

	public ListenMessage() {
	}

	public ListenMessage(boolean listen) {
		this.listen = listen;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		listen = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(listen);
	}
}
