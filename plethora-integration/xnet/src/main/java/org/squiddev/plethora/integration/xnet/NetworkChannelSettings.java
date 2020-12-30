package org.squiddev.plethora.integration.xnet;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.wired.IWiredElement;
import dan200.computercraft.api.network.wired.IWiredNode;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.ConsumerId;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.squiddev.plethora.integration.xnet.NetworkChannelType.Capabilities.WIRED_ELEMENT;

class NetworkChannelSettings implements IChannelSettings, IWiredElement {
	private boolean cleanCache = true;

	private final Map<SidedConsumer, ConnectorNode> nodes = new HashMap<>();
	private int refreshCooldown;

	private final IWiredNode channelNode = ComputerCraftAPI.createWiredNodeForElement(this);
	private World controllerWorld;
	private BlockPos controllerBlockPos = null;
	private Vec3d controllerPos = Vec3d.ZERO;

	@Override
	public void tick(int channel, IControllerContext context) {
		boolean refresh = (refreshCooldown = (refreshCooldown + 1) % 20) == 0 || cleanCache;

		if (cleanCache) {
			cleanCache = false;
			Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);

			Iterator<SidedConsumer> it = nodes.keySet().iterator();
			while (it.hasNext()) {
				SidedConsumer consumer = it.next();
				if (!connectors.containsKey(consumer)) {
					nodes.get(consumer).remove();
					it.remove();
				}
			}

			for (Map.Entry<SidedConsumer, IConnectorSettings> connector : connectors.entrySet()) {
				SidedConsumer consumer = connector.getKey();
				ConnectorNode existing = nodes.get(consumer);
				if (existing == null || existing.side != consumer.getSide()) {
					if (existing != null) existing.remove();
					nodes.put(consumer, new ConnectorNode(consumer.getConsumerId(), consumer.getSide(), (NetworkConnectorSettings) connector.getValue(), channelNode));
				}
			}
		}

		// Try to update our node's position, either from the controller tile or just borrowing a random node.
		controllerWorld = context.getControllerWorld();
		if (context instanceof TileEntity) {
			updatePosition(((TileEntity) context).getPos());
		} else if (refresh) {
			for (SidedConsumer consumer : nodes.keySet()) {
				BlockPos pos = context.findConsumerPosition(consumer.getConsumerId());
				if (pos != null) {
					updatePosition(pos);
					break;
				}
			}
		}

		// And tick each connection
		for (ConnectorNode node : nodes.values()) node.tick(context, refresh);
	}

	private void updatePosition(BlockPos pos) {
		if (!Objects.equals(controllerBlockPos, pos)) {
			controllerBlockPos = pos;
			controllerPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		}
	}


	@Override
	public void readFromNBT(NBTTagCompound tag) {
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
	}

	@Override
	public void cleanCache() {
		cleanCache = true;
	}

	@Override
	public int getColors() {
		return 0;
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		return NetworkChannelType.CHANNEL_ICON;
	}

	@Nullable
	@Override
	public String getIndicator() {
		return null;
	}

	@Override
	public boolean isEnabled(String tag) {
		return true;
	}

	@Override
	public void createGui(IEditorGui gui) {
	}

	@Override
	public void update(Map<String, Object> data) {
	}

	@Nonnull
	@Override
	public IWiredNode getNode() {
		return channelNode;
	}

	@Nonnull
	@Override
	public World getWorld() {
		return controllerWorld;
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return controllerPos;
	}

	@Nonnull
	@Override
	public String getSenderID() {
		return "xnet";
	}

	private static final class ConnectorNode {
		private final ConsumerId id;
		private final EnumFacing side;
		private final NetworkConnectorSettings settings;
		private final IWiredNode rootNode;

		private TileEntity tile;
		private IWiredNode node;

		private ConnectorNode(ConsumerId id, EnumFacing side, NetworkConnectorSettings settings, IWiredNode rootNode) {
			this.id = id;
			this.side = side;
			this.settings = settings;
			this.rootNode = rootNode;
		}

		void remove() {
			IWiredNode node = this.node;
			tile = null;

			if (node != null) {
				node.disconnectFrom(rootNode);
				this.node = null;
			}
		}

		void tick(IControllerContext context, boolean refresh) {
			if (node != null && ((tile != null && tile.isInvalid()) || !context.matchColor(settings.getColorsMask()))) {
				remove();
			}

			if (!refresh) return;

			// We're enabled and should refresh: fetch the tile (if needed), and re-fetch the node.
			BlockPos pos = context.findConsumerPosition(id);
			if (tile == null) tile = pos != null ? context.getControllerWorld().getTileEntity(pos.offset(side)) : null;

			IWiredElement element = tile != null && tile.hasCapability(WIRED_ELEMENT, settings.getFacing())
				? tile.getCapability(WIRED_ELEMENT, settings.getFacing()) : null;
			IWiredNode newNode = element == null ? null : element.getNode();
			if (newNode != node) {
				if (node != null) node.disconnectFrom(rootNode);
				node = newNode;
				if (newNode != null) newNode.connectTo(rootNode);
			}
		}
	}
}
