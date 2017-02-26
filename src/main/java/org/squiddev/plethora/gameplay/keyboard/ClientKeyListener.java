package org.squiddev.plethora.gameplay.keyboard;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.ArrayList;
import java.util.List;

import static org.squiddev.plethora.gameplay.keyboard.KeyMessage.KeyPress;

public class ClientKeyListener implements IMessageHandler<ListenMessage, IMessage> {
	private boolean listen;
	private final ArrayList<KeyDown> keysDown = Lists.newArrayList();

	private final List<KeyPress> keyPresses = Lists.newArrayList();
	private final List<Integer> keysUp = Lists.newArrayList();

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onInputEvent(InputEvent.KeyInputEvent event) {
		if (!listen) {
			// If we're not listening then clear the lookup of keys which are down and exit.
			keysDown.clear();
			return;
		}

		if (Keyboard.getEventKeyState()) {
			char ch = Keyboard.getEventCharacter();
			int key = Keyboard.getEventKey();

			boolean repeat = Keyboard.isRepeatEvent();

			ch = ch >= 32 && ch <= 126 || ch >= 160 && ch <= 255 ? ch : '\0';

			if (key > 0 || ch != '\0') {
				keyPresses.add(new KeyPress(key, repeat, ch));

				boolean found = false;
				for (KeyDown down : keysDown) {
					if (down.key == key) {
						down.lastTime = Minecraft.getSystemTime();
						found = true;
						break;
					}
				}

				if (!found) keysDown.add(new KeyDown(key, ch));
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;

		for (int i = keysDown.size() - 1; i >= 0; --i) {
			KeyDown down = keysDown.get(i);
			if (Keyboard.isKeyDown(down.key)) {
				// Emulate repeat presses. We simply fire a repeat event every 1/10th of a second
				// This is far from ideal, but is "good enough" for most practical uses

				long time = Minecraft.getSystemTime();
				if (down.lastTime <= time - 100) {
					down.lastTime = time;
					keyPresses.add(new KeyPress(down.key, true, down.character));
				}
			} else {
				keysDown.remove(i);
				keysUp.add(down.key);
			}
		}

		if (keyPresses.size() > 0 || keysUp.size() > 0) {
			Plethora.network.sendToServer(new KeyMessage(keyPresses, keysUp));
			keyPresses.clear();
			keysUp.clear();
		}
	}

	@Override
	public IMessage onMessage(ListenMessage message, MessageContext ctx) {
		listen = message.listen;
		return null;
	}

	private static final class KeyDown {
		public final int key;
		public final char character;

		public long lastTime;

		private KeyDown(int key, char character) {
			this.key = key;
			this.character = character;
			this.lastTime = Minecraft.getSystemTime();
		}
	}
}
