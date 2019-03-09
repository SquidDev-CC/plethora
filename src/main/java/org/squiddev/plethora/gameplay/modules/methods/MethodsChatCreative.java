package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.gen.FromContext;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static org.squiddev.plethora.gameplay.modules.ChatListener.Listener;
import static org.squiddev.plethora.gameplay.modules.methods.MethodsChat.validateMessage;

public final class MethodsChatCreative {
	@PlethoraMethod(
		module = PlethoraModules.CHAT_CREATIVE_S,
		doc = "-- Send a message to everyone"
	)
	public static void say(
		@Nonnull final IContext<IModuleContainer> context,
		@FromContext(ContextKeys.ORIGIN) @Nullable IWorldLocation location,
		String message
	) throws LuaException {
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

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_CREATIVE_S, target = Listener.class, worldThread = false,
		doc = "function(pattern:string) -- Capture all chat messages matching a Lua pattern, preventing them from being said."
	)
	@Nullable
	public static Object[] capture(Listener listener, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		String pattern = getString(args, 0);
		listener.addPattern(pattern);
		return null;
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_CREATIVE_S, target = Listener.class, worldThread = false,
		doc = "function(pattern:string):boolean -- Remove a capture added by capture(pattern)."
	)
	@Nonnull
	public static Object[] uncapture(Listener listener, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		String pattern = getString(args, 0);
		boolean removed = listener.removePattern(pattern);
		return new Object[]{removed};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_CREATIVE_S, target = Listener.class, worldThread = false,
		doc = "function() -- Remove all listeners added by capture()."
	)
	@Nullable
	public static Object[] clearCaptures(Listener listener, @Nonnull final IContext<IModuleContainer> unbaked, @Nonnull Object[] args) {
		listener.clearPatterns();
		return null;
	}
}
