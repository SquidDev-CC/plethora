package org.squiddev.plethora.gameplay.modules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.registry.Registration;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.LuaPattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Plethora.ID)
public final class ChatListener {
	private static Set<Listener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private ChatListener() {
	}

	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		Entity sender = event.getPlayer();
		if (sender instanceof PlethoraFakePlayer) {
			Entity owner = ((PlethoraFakePlayer) sender).getOwner();
			sender = owner == null ? sender : owner;
		}

		// Handle captures
		for (Listener listener : listeners) {
			if (listener.handles(sender) && listener.handleCapture(sender, event.getMessage())) {
				event.setCanceled(true);
				return;
			}
		}

		// Handle chat messages for everyone
		for (Listener listener : listeners) {
			listener.handleMessage(sender, event.getMessage());
		}

		// And send a message to every player in range holding a chat recorder.
		PlayerList players = event.getPlayer().server.getPlayerList();
		int distance = players.getViewDistance() * 16;
		distance *= distance;
		ChatMessage message = new ChatMessage(sender, event.getComponent());

		for (EntityPlayerMP player : players.getPlayers()) {
			if (Helpers.isHolding(player, Registration.itemModule, PlethoraModules.CHAT_ID) ||
				Helpers.isHolding(player, Registration.itemModule, PlethoraModules.CHAT_CREATIVE_ID)) {
				if (player != sender && player.getEntityWorld() == sender.getEntityWorld() && player.getDistanceSq(sender) <= distance) {
					Plethora.network.sendTo(message, player);
				}
			}
		}
	}

	public static class Listener implements IAttachable, ConstantReference<Listener> {
		private final IModuleAccess access;
		private final UUID owner;
		private final Set<String> patterns = new HashSet<>();

		public Listener(@Nonnull IModuleAccess access, @Nullable UUID owner) {
			this.access = access;
			this.owner = owner;
		}

		public boolean handles(Entity sender) {
			return owner != null && owner.equals(sender.getUniqueID());
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

		private synchronized boolean handleCapture(Entity sender, String message) {
			for (String pattern : patterns) {
				if (LuaPattern.matches(message, pattern)) {
					access.queueEvent("chat_capture", message, pattern, sender.getDisplayName().getUnformattedText(), sender.getPersistentID().toString());
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
		public Listener get() {
			return this;
		}

		@Nonnull
		@Override
		public Listener safeGet() {
			return this;
		}
	}

	public static class CreativeListener extends Listener {
		public CreativeListener(@Nonnull IModuleAccess access) {
			super(access, null);
		}

		@Override
		public boolean handles(Entity sender) {
			return true;
		}
	}
}
