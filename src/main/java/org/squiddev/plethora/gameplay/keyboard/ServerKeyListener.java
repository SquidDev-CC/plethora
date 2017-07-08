package org.squiddev.plethora.gameplay.keyboard;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.WeakHashMap;

import static org.squiddev.plethora.gameplay.keyboard.KeyMessage.KeyPress;

public class ServerKeyListener implements IMessageHandler<KeyMessage, IMessage> {
	private static final WeakHashMap<EntityPlayerMP, Set<IModuleAccess>> listeners = new WeakHashMap<EntityPlayerMP, Set<IModuleAccess>>();

	public static void add(@Nonnull EntityPlayerMP player, @Nonnull IModuleAccess access) {
		synchronized (listeners) {
			Set<IModuleAccess> accesses = listeners.get(player);
			if (accesses == null) listeners.put(player, accesses = Sets.newHashSet());

			// Notify the client to start listening
			if (accesses.isEmpty()) {
				Plethora.network.sendTo(new ListenMessage(true), player);
			}

			accesses.add(access);
		}
	}

	public static void remove(@Nonnull EntityPlayerMP player, @Nonnull IModuleAccess access) {
		synchronized (listeners) {
			Set<IModuleAccess> accesses = listeners.get(player);
			if (accesses == null) return;

			// Notify the client to start listening
			if (accesses.remove(access) && accesses.isEmpty()) {
				Plethora.network.sendTo(new ListenMessage(false), player);
			}
		}
	}

	public static void clear() {
		synchronized (listeners) {
			listeners.clear();
		}
	}

	@Override
	public IMessage onMessage(KeyMessage message, MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;

		synchronized (listeners) {
			Set<IModuleAccess> accesses = listeners.get(player);
			if (accesses != null && accesses.size() > 0) {
				for (IModuleAccess access : accesses) {
					for (KeyPress press : message.presses) {
						if (press.key > 0) access.queueEvent("key", press.key, press.repeat);
						if (press.ch != '\0') access.queueEvent("char", Character.toString(press.ch));
					}

					for (Integer up : message.ups) {
						access.queueEvent("key_up", up);
					}
				}
			}
		}

		return null;
	}
}
