package org.squiddev.plethora.gameplay.keyboard;

import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import static org.squiddev.plethora.gameplay.keyboard.KeyMessage.KeyPress;

public final class ServerKeyListener {
	private static final WeakHashMap<EntityPlayerMP, Set<IModuleAccess>> listeners = new WeakHashMap<>();

	private ServerKeyListener() {
	}

	public static void add(@Nonnull EntityPlayerMP player, @Nonnull IModuleAccess access) {
		synchronized (listeners) {
			Set<IModuleAccess> accesses = listeners.computeIfAbsent(player, k -> new HashSet<>(1));

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

	static void process(EntityPlayerMP player, List<KeyPress> presses, List<Integer> releases) {
		synchronized (listeners) {
			Set<IModuleAccess> accesses = listeners.get(player);
			if (accesses != null && !accesses.isEmpty()) {
				for (IModuleAccess access : accesses) {
					for (KeyPress press : presses) {
						if (press.key > 0) access.queueEvent("key", press.key, press.repeat);
						if (press.ch != '\0') access.queueEvent("char", Character.toString(press.ch));
					}

					for (Integer up : releases) {
						access.queueEvent("key_up", up);
					}
				}
			}
		}
	}
}
