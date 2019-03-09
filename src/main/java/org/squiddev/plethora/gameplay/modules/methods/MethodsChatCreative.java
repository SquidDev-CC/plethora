package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.gen.FromContext;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nullable;

import static org.squiddev.plethora.gameplay.modules.ChatListener.Listener;
import static org.squiddev.plethora.gameplay.modules.methods.MethodsChat.validateMessage;

public final class MethodsChatCreative {
	@PlethoraMethod(module = PlethoraModules.CHAT_CREATIVE_S, doc = "-- Send a message to everyone")
	public static void say(@Nullable @FromContext(ContextKeys.ORIGIN) IWorldLocation location, String message) throws LuaException {
		validateMessage(message);

		// Create the chat event and post to chat
		ITextComponent formatted = ForgeHooks.newChatWithLinks(message);

		// Attempt to extract the server from the current world.
		MinecraftServer server = null;
		if (location != null) server = location.getWorld().getMinecraftServer();

		// If that failed then just get the global server.
		if (server == null) server = FMLCommonHandler.instance().getMinecraftServerInstance();

		server.getPlayerList().sendMessage(formatted, false);
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_CREATIVE_S, worldThread = false,
		doc = "-- Capture all chat messages matching a Lua pattern, preventing them from being said."
	)
	public static void capture(@FromContext(PlethoraModules.CHAT_CREATIVE_S) Listener listener, String pattern) {
		listener.addPattern(pattern);
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_CREATIVE_S, worldThread = false,
		doc = "-- Remove a capture added by capture(pattern)."
	)
	public static boolean uncapture(@FromContext(PlethoraModules.CHAT_CREATIVE_S) Listener listener, String pattern) {
		return listener.removePattern(pattern);
	}

	@PlethoraMethod(
		module = PlethoraModules.CHAT_CREATIVE_S, worldThread = false,
		doc = "-- Remove all listeners added by capture()."
	)
	public static void clearCaptures(@FromContext(PlethoraModules.CHAT_CREATIVE_S) Listener listener) {
		listener.clearPatterns();
	}
}
