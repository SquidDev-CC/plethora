package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.utils.LuaPattern;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener extends Module {
	private static Set<Listener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<Listener, Boolean>());

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onServerChat(ServerChatEvent event) {
		Entity sender = event.getPlayer();
		if (sender instanceof PlethoraFakePlayer) {
			Entity owner = ((PlethoraFakePlayer) sender).getOwner();
			sender = owner == null ? sender : owner;
		}

		for (Listener listener : listeners) {
			if (listener.owner == sender) {
				if (listener.handle(event.getMessage())) {
					event.setCanceled(true);
				}
			}
		}
	}

	public static class Listener implements IAttachable, IReference<Listener> {
		private final IModuleAccess access;
		private final Entity owner;
		private final Set<String> patterns = Sets.newHashSet();

		public Listener(IModuleAccess access, Entity owner) {
			this.access = access;
			this.owner = owner;
		}

		public void addPattern(String pattern) {
			patterns.add(pattern);
		}

		public boolean removePattern(String pattern) {
			return patterns.remove(pattern);
		}

		public void clearPatterns() {
			patterns.clear();
		}

		private boolean handle(String message) {
			access.queueEvent("chat_message", message);

			for (String pattern : patterns) {
				if (LuaPattern.matches(message, pattern)) {
					access.queueEvent("chat_capture", message, pattern);
					return true;
				}
			}

			return false;
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
	}
}
