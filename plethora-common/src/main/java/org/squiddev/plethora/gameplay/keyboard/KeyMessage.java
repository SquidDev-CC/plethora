package org.squiddev.plethora.gameplay.keyboard;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

import java.util.List;

public class KeyMessage implements BasicMessage {
	public static class KeyPress {
		public final int key;
		public final boolean repeat;
		public final char ch;

		public KeyPress(int key, boolean repeat, char ch) {
			this.key = key;
			this.repeat = repeat;
			this.ch = ch;
		}
	}

	private List<KeyPress> presses;
	private List<Integer> ups;

	public KeyMessage() {
	}

	public KeyMessage(List<KeyPress> presses, List<Integer> ups) {
		this.presses = Lists.newArrayList(presses);
		this.ups = Lists.newArrayList(ups);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int pressSize = buf.readInt();
		presses = Lists.newArrayListWithCapacity(pressSize);
		for (int i = 0; i < pressSize; i++) {
			int key = buf.readInt();
			boolean repeat = buf.readBoolean();
			char ch = buf.readChar();

			presses.add(new KeyPress(key, repeat, ch));
		}

		int upSize = buf.readInt();
		ups = Lists.newArrayListWithCapacity(upSize);
		for (int i = 0; i < upSize; i++) {
			ups.add(buf.readInt());
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(presses.size());
		for (KeyPress press : presses) {
			buf.writeInt(press.key);
			buf.writeBoolean(press.repeat);
			buf.writeChar(press.ch);
		}

		buf.writeInt(ups.size());
		for (Integer up : ups) {
			buf.writeInt(up);
		}
	}

	@Override
	public void onMessage(MessageContext ctx) {
		ServerKeyListener.process(ctx.getServerHandler().player, presses, ups);
	}
}
