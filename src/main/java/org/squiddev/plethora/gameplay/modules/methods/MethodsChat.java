package org.squiddev.plethora.gameplay.modules.methods;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;
import org.squiddev.plethora.integration.vanilla.FakePlayerProviderEntity;
import org.squiddev.plethora.integration.vanilla.FakePlayerProviderLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.gameplay.modules.ChatListener.Listener;

public final class MethodsChat {
	@PlethoraMethod(module = PlethoraModules.CHAT_S, doc = "-- Send a message to everyone")
	public static void say(
		@Nonnull IContext<IModuleContainer> context,
		@FromContext Entity entity, // TODO: EntityIdentifier?
		@FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		String message
	) throws LuaException {
		validateMessage(message);

		@Nullable
		GameProfile moduleProfile = null;

		if (ConfigGameplay.Chat.allowBinding) {
			// If we allow binding the neural interface, fetch the entity info from the module location instead.
			if (entity == null) entity = context.getContext(PlethoraModules.CHAT_S, Entity.class);

			IPlayerOwnable moduleOwner = context.getContext(PlethoraModules.CHAT_S, IPlayerOwnable.class);
			if (ConfigGameplay.Chat.allowOffline && moduleOwner != null) {
				moduleProfile = moduleOwner.getOwningProfile();
			}
		}


		EntityPlayerMP player;
		ITextComponent name;

		// Attempt to guess who is posting it and their position.
		if (entity instanceof EntityPlayerMP) {
			// If we've got some player, go ahead as normal
			name = entity.getDisplayName();
			player = (EntityPlayerMP) entity;
		} else if (ConfigGameplay.Chat.allowMobs && entity != null && entity.getEntityWorld() instanceof WorldServer) {
			IPlayerOwnable ownable = context.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
			GameProfile owner = ownable == null ? null : ownable.getOwningProfile();
			if (owner == null) owner = PlethoraFakePlayer.PROFILE;// We include the position of the entity

			name = entity.getDisplayName().createCopy();

			PlethoraFakePlayer fakePlayer = new PlethoraFakePlayer((WorldServer) entity.getEntityWorld(), entity, owner);
			FakePlayerProviderEntity.load(fakePlayer, entity);
			player = fakePlayer;
		} else if (moduleProfile != null && location != null && location.getWorld() instanceof WorldServer) {
			// If we've got a location and a game profile _associated with this module_ then we use that
			PlethoraFakePlayer fakePlayer = new PlethoraFakePlayer((WorldServer) location.getWorld(), null, moduleProfile);
			fakePlayer.setCustomNameTag(moduleProfile.getName());
			FakePlayerProviderLocation.load(fakePlayer, location);
			player = fakePlayer;

			name = fakePlayer.getDisplayName();
		} else {
			throw new LuaException("Cannot post to chat");
		}

		// Create the chat event and post to chat
		TextComponentTranslation translateChat = new TextComponentTranslation("chat.type.text", name, ForgeHooks.newChatWithLinks(message));
		ServerChatEvent event = new ServerChatEvent(player, message, translateChat);
		if (MinecraftForge.EVENT_BUS.post(event) || event.getComponent() == null) return;

		player.server.getPlayerList().sendMessage(event.getComponent(), false);
	}

	@PlethoraMethod(module = PlethoraModules.CHAT_S, doc = "-- Send a message to yourself")
	public static void tell(@FromSubtarget EntityIdentifier entity, String message) throws LuaException {
		validateMessage(message);
		EntityLivingBase sender = entity.getEntity();
		sender.sendMessage(ForgeHooks.newChatWithLinks(message));
	}

	static void validateMessage(String message) throws LuaException {
		if (ConfigGameplay.Chat.maxLength > 0 && message.length() > ConfigGameplay.Chat.maxLength) {
			throw new LuaException(String.format("Message is too long (was %d, maximum is %d)", message.length(), ConfigGameplay.Chat.maxLength));
		}

		for (int i = 0; i < message.length(); ++i) {
			char character = message.charAt(i);
			if (character < 32 || character == 127 || (character == 167 && !ConfigGameplay.Chat.allowFormatting)) {
				throw new LuaException("Illegal character '" + message.charAt(i) + "'");
			}
		}
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_S, worldThread = false,
		doc = "-- Capture all chat messages matching a Lua pattern, preventing them from being said."
	)
	public static void capture(@FromContext(PlethoraModules.CHAT_S) Listener listener, String pattern) {
		listener.addPattern(pattern);
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_S, worldThread = false,
		doc = "-- Remove a capture added by capture(pattern)."
	)
	public static boolean uncapture(@FromContext(PlethoraModules.CHAT_S) Listener listener, String pattern) {
		return listener.removePattern(pattern);
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_S, worldThread = false,
		doc = "-- Remove all listeners added by capture()."
	)
	public static void clearCaptures(@FromContext(PlethoraModules.CHAT_S) Listener listener) {
		listener.clearPatterns();
	}
}
