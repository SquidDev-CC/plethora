package org.squiddev.plethora.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import net.minecraft.network.play.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.FakePlayer;

import javax.crypto.SecretKey;

public class FakeNetHandler extends NetHandlerPlayServer {
	public static class FakeNetworkManager extends NetworkManager {
		public FakeNetworkManager() {
			super(EnumPacketDirection.CLIENTBOUND);
		}

		@Override
		public void channelActive(ChannelHandlerContext context) throws Exception {
		}

		@Override
		public void setConnectionState(EnumConnectionState state) {
		}

		@Override
		public void channelInactive(ChannelHandlerContext context) {
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext context, Throwable e) {
		}

		@Override
		public void setNetHandler(INetHandler handler) {
		}

		@Override
		public void processReceivedPackets() {
		}

		@Override
		public void closeChannel(IChatComponent channel) {
		}

		@Override
		public boolean isLocalChannel() {
			return false;
		}


		@Override
		public void enableEncryption(SecretKey key) {
		}

		@Override
		public boolean isChannelOpen() {
			return false;
		}

		@Override
		public INetHandler getNetHandler() {
			return null;
		}

		@Override
		public IChatComponent getExitMessage() {
			return null;
		}

		@Override
		public void disableAutoRead() {
		}

		@Override
		public Channel channel() {
			return null;
		}
	}


	public FakeNetHandler(FakePlayer player) {
		this(player.mcServer, player);
	}

	public FakeNetHandler(MinecraftServer server, FakePlayer player) {
		super(server, new FakeNetworkManager(), player);
	}

	@Override
	public void kickPlayerFromServer(String player) {
	}

	@Override
	public void processInput(C0CPacketInput packet) {
	}

	@Override
	public void processPlayer(C03PacketPlayer packet) {
	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
	}

	@Override
	public void processPlayerDigging(C07PacketPlayerDigging packet) {
	}

	@Override
	public void processPlayerBlockPlacement(C08PacketPlayerBlockPlacement packet) {
	}

	@Override
	public void onDisconnect(IChatComponent chat) {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void sendPacket(final Packet packet) {

	}

	@Override
	public void processHeldItemChange(C09PacketHeldItemChange packet) {
	}

	@Override
	public void processChatMessage(C01PacketChatMessage packet) {
	}

	@Override
	public void processEntityAction(C0BPacketEntityAction packet) {
	}

	@Override
	public void processUseEntity(C02PacketUseEntity packet) {
	}

	@Override
	public void processClientStatus(C16PacketClientStatus packet) {
	}

	@Override
	public void processCloseWindow(C0DPacketCloseWindow packet) {
	}

	@Override
	public void processClickWindow(C0EPacketClickWindow packet) {
	}

	@Override
	public void processEnchantItem(C11PacketEnchantItem packet) {
	}

	@Override
	public void processCreativeInventoryAction(C10PacketCreativeInventoryAction packet) {
	}

	@Override
	public void processConfirmTransaction(C0FPacketConfirmTransaction packet) {
	}

	@Override
	public void processUpdateSign(C12PacketUpdateSign packet) {
	}

	@Override
	public void processKeepAlive(C00PacketKeepAlive packet) {
	}

	@Override
	public void processPlayerAbilities(C13PacketPlayerAbilities packet) {
	}

	@Override
	public void processTabComplete(C14PacketTabComplete packet) {
	}

	@Override
	public void processClientSettings(C15PacketClientSettings packet) {
	}

	@Override
	public void processVanilla250Packet(C17PacketCustomPayload packet) {
	}
}
