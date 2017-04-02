package org.squiddev.plethora.gameplay.registry;

import org.squiddev.plethora.gameplay.minecart.MessageMinecartSlot;

/**
 * Simple lookup of all packet IDs
 */
public final class Packets {
	private Packets() {
	}

	/**
	 * @see org.squiddev.plethora.gameplay.modules.ChatVisualiser
	 * @see org.squiddev.plethora.gameplay.modules.ChatVisualiser.ChatMessage
	 */
	public static final int CHAT_MESSAGE = 0;

	/**
	 * @see org.squiddev.plethora.gameplay.keyboard.ServerKeyListener
	 * @see org.squiddev.plethora.gameplay.keyboard.KeyMessage
	 */
	public static final int KEY_MESSAGE = 1;

	/**
	 * @see org.squiddev.plethora.gameplay.keyboard.ClientKeyListener
	 * @see org.squiddev.plethora.gameplay.keyboard.ListenMessage
	 */
	public static final int LISTEN_MESSAGE = 2;

	/**
	 * @see org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer
	 * @see MessageMinecartSlot
	 */
	public static final int MINECART_MESSAGE = 3;
}
