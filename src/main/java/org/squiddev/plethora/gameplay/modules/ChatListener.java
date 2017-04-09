package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.utils.LuaPattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener extends Module {
	private static Set<Listener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<Listener, Boolean>());

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onServerChat(ServerChatEvent event) {
		Entity sender = event.getPlayer();
		if (sender instanceof PlethoraFakePlayer) {
			Entity owner = ((PlethoraFakePlayer) sender).getOwner();
			sender = owner == null ? sender : owner;
		}

		// Handle captures
		for (Listener listener : listeners) {
			if (listener.owner != null && listener.owner == sender) {
				if (listener.handleCapture(event.getMessage())) {
					event.setCanceled(true);
					return;
				}
			}
		}

		// Handle chat messages for everyone
		for (Listener listener : listeners) {
			listener.handleMessage(sender, event.getMessage());
		}
	}

	public static class Listener implements IAttachable, IReference<Listener> {
		private final IModuleAccess access;
		private final Entity owner;
		private final Set<String> patterns = Sets.newHashSet();

		public Listener(@Nonnull IModuleAccess access, @Nullable Entity owner) {
			this.access = access;
			this.owner = owner;
		}

		public synchronized void addPattern(String pattern) {
			patterns.add(pattern);
		}

		public synchronized boolean removePattern(String pattern) {
			return patterns.remove(pattern);
		}

		public synchronized void clearPatterns() {
			patterns.clear();
		}

		private synchronized boolean handleCapture(String message) {
			for (String pattern : patterns) {
				if (LuaPattern.matches(message, pattern)) {
					access.queueEvent("chat_capture", message, pattern);
					return true;
				}
			}

			return false;
		}

		private void handleMessage(Entity sender, String message) {
			access.queueEvent("chat_message", sender.getDisplayName().getUnformattedText(), message, sender.getPersistentID().toString());
		}

		@Override
		public void attach() {
			listeners.add(this);
		}

		@Override
		public void detach() {
			listeners.remove(this);
		}

		@Nonnull
		@Override
		public Listener get() throws LuaException {
			return this;
		}

		@Nonnull
		@Override
		public Listener safeGet() throws LuaException {
			return this;
		}
	}
}
