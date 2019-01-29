package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleContainerMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static org.squiddev.plethora.gameplay.modules.ChatListener.Listener;
import static org.squiddev.plethora.gameplay.modules.methods.MethodsChat.validateMessage;

public final class MethodsChatCreative {
	@ModuleContainerMethod.Inject(
		value = PlethoraModules.CHAT_CREATIVE_S,
		doc = "function(message:string) -- Send a message to everyone"
	)
	@Nonnull
	public static MethodResult say(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull Object[] args) throws LuaException {
		final String message = getString(args, 0);
		validateMessage(message);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> context = unbaked.bake();

				// Create the chat event and post to chat
				ITextComponent formatted = ForgeHooks.newChatWithLinks(message);

				// Attempt to extract the server from the current world.
				MinecraftServer server = null;
				if (context.hasContext(ContextKeys.ORIGIN, IWorldLocation.class)) {
					server = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class).getWorld().getMinecraftServer();
				}

				// If that failed then just get the global server.
				if (server == null) server = FMLCommonHandler.instance().getMinecraftServerInstance();

				server.getPlayerList().sendMessage(formatted, false);
				return MethodResult.empty();
			}
		});
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
