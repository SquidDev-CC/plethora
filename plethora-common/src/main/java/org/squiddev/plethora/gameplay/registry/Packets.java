package org.squiddev.plethora.gameplay.registry;

import org.squiddev.plethora.gameplay.minecart.MessageMinecartSlot;

/**
 * Simple lookup of all packet IDs
 */
public final class Packets {
	private Packets() {
	}

	/**
	 * @see org.squiddev.plethora.gameplay.client.RenderOverlay
	 * @see org.squiddev.plethora.gameplay.modules.ChatMessage
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

	/**
	 * @see org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasAdd
	 * @see org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler
	 */
	public static final int CANVAS_ADD_MESSAGE = 4;

	/**
	 * @see org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasRemove
	 * * @see org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler
	 */
	public static final int CANVAS_REMOVE_MESSAGE = 5;

	/**
	 * @see org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasUpdate
	 * * @see org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler
	 */
	public static final int CANVAS_UPDATE_MESSAGE = 6;
}
