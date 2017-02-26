package org.squiddev.plethora.gameplay.keyboard;

import com.google.common.collect.Lists;
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

import java.util.ArrayList;
import java.util.List;

import static org.squiddev.plethora.gameplay.keyboard.KeyMessage.KeyPress;

public class ClientKeyListener implements IMessageHandler<ListenMessage, IMessage> {
	private boolean listen;
	private final ArrayList<Integer> keysDown = Lists.newArrayList();

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

		/*
			It's worth noting that we don't actually receive repeat events which is a shame.
			It might be worth finding a work around in the future: can we find the keyboard's delay period and
			emulate repeat events?
		  */

		if (Keyboard.getEventKeyState()) {
			char ch = Keyboard.getEventCharacter();
			int key = Keyboard.getEventKey();

			boolean repeat = Keyboard.isRepeatEvent();
			if (key <= 0) {
				key = 0;
			} else if (!repeat) {
				keysDown.add(key);
			}

			ch = ch >= 32 && ch <= 126 || ch >= 160 && ch <= 255 ? ch : '\0';

			if (key != 0 || ch != '\0') keyPresses.add(new KeyPress(key, repeat, ch));
		}

		for (int i = keysDown.size() - 1; i >= 0; --i) {
			int key = keysDown.get(i);
			if (!Keyboard.isKeyDown(key)) {
				keysDown.remove(i);
				keysUp.add(key);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;

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
}
