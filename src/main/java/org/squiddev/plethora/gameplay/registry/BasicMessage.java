package org.squiddev.plethora.gameplay.registry;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Extension to {@link IMessage} which can be handled by {@link #defaultHandler(BasicMessage, MessageContext)}.
 */
public interface BasicMessage extends IMessage {
	void onMessage(MessageContext context);

	static <T extends BasicMessage> IMessage defaultHandler(T message, MessageContext context) {
		message.onMessage(context);
		return null;
	}
}
