package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getString;
import static org.squiddev.plethora.gameplay.modules.ChatListener.Listener;

public final class MethodsChat {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.CHAT_S, target = Entity.class,
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
				Entity entity = context.getContext(Entity.class);

				EntityPlayerMP player;
				IChatComponent name;

				// Attempt to guess who is posting it and their position.
				if (entity instanceof EntityPlayerMP) {
					name = entity.getDisplayName();
					player = (EntityPlayerMP) entity;

				} else if (entity.worldObj instanceof WorldServer) {
					if (!ConfigGameplay.Chat.allowMobs) throw new LuaException("Mobs cannot post to chat");

					BlockPos pos = entity.getPosition();

					// We include the position of the entity
					name = entity.getDisplayName().createCopy();
					name.appendText(String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ()));
					PlethoraFakePlayer fakePlayer = new PlethoraFakePlayer((WorldServer) entity.worldObj, entity, name.getUnformattedText());
					fakePlayer.load(entity);
					player = fakePlayer;
				} else {
					throw new LuaException("Cannot post to chat");
				}

				// Create the chat event and post to chat
				ChatComponentTranslation translateChat = new ChatComponentTranslation("chat.type.text", name, ForgeHooks.newChatWithLinks(message));
				ServerChatEvent event = new ServerChatEvent(player, message, translateChat);
				if (MinecraftForge.EVENT_BUS.post(event) || event.getComponent() == null) return MethodResult.empty();

				player.mcServer.getConfigurationManager().sendChatMsgImpl(event.getComponent(), false);
				return MethodResult.empty();
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.CHAT_S, target = Entity.class,
		doc = "function(message:string) -- Send a message to yourself"
	)
	@Nonnull
	public static MethodResult tell(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull Object[] args) throws LuaException {
		final String message = getString(args, 0);
		validateMessage(message);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> context = unbaked.bake();
				Entity entity = context.getContext(Entity.class);

				entity.addChatMessage(ForgeHooks.newChatWithLinks(message));
				return MethodResult.empty();
			}
		});
	}

	private static void validateMessage(String message) throws LuaException {
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

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_S, target = Listener.class, worldThread = false,
		doc = "function(pattern:string) -- Capture all chat messages matching a Lua pattern, preventing them from being said."
	)
	@Nullable
	public static Object[] capture(Listener listener, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		String pattern = getString(args, 0);
		listener.addPattern(pattern);
		return null;
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_S, target = Listener.class, worldThread = false,
		doc = "function(pattern:string):boolean -- Remove a capture added by capture(pattern)."
	)
	@Nonnull
	public static Object[] uncapture(Listener listener, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		String pattern = getString(args, 0);
		boolean removed = listener.removePattern(pattern);
		return new Object[]{removed};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.CHAT_S, target = Listener.class, worldThread = false,
		doc = "function() -- Remove all listeners added by capture()."
	)
	@Nullable
	public static Object[] clearCaptures(Listener listener, @Nonnull final IContext<IModuleContainer> unbaked, @Nonnull Object[] args) throws LuaException {
		listener.clearPatterns();
		return null;
	}
}
