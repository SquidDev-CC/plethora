package org.squiddev.plethora.gameplay.modules;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.RenderOverlay;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.gameplay.registry.Packets;
import org.squiddev.plethora.gameplay.registry.Registry;
import org.squiddev.plethora.utils.DebugLogger;

public class ChatVisualiser extends Module implements IMessageHandler<ChatVisualiser.ChatMessage, IMessage> {
	@Override
	public void preInit() {
		Plethora.network.registerMessage(this, ChatMessage.class, Packets.CHAT_MESSAGE, Side.CLIENT);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onServerChat(ServerChatEvent event) {
		ServerConfigurationManager players = event.player.mcServer.getConfigurationManager();
		int distance = (players.getViewDistance() * 16);
		distance *= distance;

		EntityPlayerMP sender = event.player;
		ChatMessage message = new ChatMessage(sender, event.getComponent());

		for (EntityPlayerMP player : players.getPlayerList()) {
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() == Registry.itemModule && stack.getMetadata() == ItemModule.CHAT_ID) {
				DebugLogger.debug("Holding chat");
				if (player != sender && player.worldObj == sender.worldObj && player.getDistanceToEntity(sender) <= distance) {
					DebugLogger.debug("Sending to player");
					Plethora.network.sendTo(message, player);
				}
			}
		}
	}

	@Override
	public IMessage onMessage(ChatMessage message, MessageContext ctx) {
		RenderOverlay.addMessage(message);
		return null;
	}

	public static final class ChatMessage implements IMessage {
		public static final int TIME = 30;

		private int world;
		private Vec3 pos;
		private String message;

		// Client side methods
		private int count = TIME;
		private int id;

		public ChatMessage(Entity entity, IChatComponent message) {
			this(
				entity.worldObj.provider.getDimensionId(),
				new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
				message
			);
		}

		public ChatMessage(int world, Vec3 pos, IChatComponent message) {
			setup(world, pos, message.getFormattedText());
		}

		public ChatMessage() {
		}

		private void setup(int world, Vec3 pos, String message) {
			this.world = world;
			this.pos = pos;
			this.message = message;

			id = pos.hashCode() * 31 + message.hashCode();
		}

		public boolean decrement() {
			return --count <= 0;
		}

		public int getWorld() {
			return world;
		}

		public Vec3 getPosition() {
			return pos;
		}

		public String getMessage() {
			return message;
		}

		public int getId() {
			return id;
		}

		public int getCount() {
			return count;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			setup(
				buf.readInt(),
				new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
				ByteBufUtils.readUTF8String(buf)
			);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(world);
			buf.writeDouble(pos.xCoord);
			buf.writeDouble(pos.yCoord);
			buf.writeDouble(pos.zCoord);
			ByteBufUtils.writeUTF8String(buf, message);
		}
	}
}
