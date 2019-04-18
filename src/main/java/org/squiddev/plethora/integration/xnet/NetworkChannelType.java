package org.squiddev.plethora.integration.xnet;

import dan200.computercraft.api.network.wired.IWiredElement;
import mcjty.xnet.api.IXNet;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IChannelType;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.gui.IndicatorIcon;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

import static org.squiddev.plethora.integration.xnet.NetworkChannelType.Capabilities.WIRED_ELEMENT;

class NetworkChannelType implements IChannelType {
	private static final String ID = "plethora.cc";
	static final IndicatorIcon CHANNEL_ICON = new IndicatorIcon(new ResourceLocation(Plethora.ID, "textures/gui/xnet.png"), 0, 0, 11, 10);
	static final IndicatorIcon CONNECTOR_ICON = new IndicatorIcon(new ResourceLocation(Plethora.ID, "textures/gui/xnet.png"), 12, 0, 13, 10);

	private static final NetworkChannelType INSTANCE = new NetworkChannelType();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return "ComputerCraft";
	}

	@Override
	public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		return tile != null && WIRED_ELEMENT != null && tile.hasCapability(WIRED_ELEMENT, side);
	}

	@Nonnull
	@Override
	public IConnectorSettings createConnector(@Nonnull EnumFacing side) {
		return new NetworkConnectorSettings(side);
	}

	@Nonnull
	@Override
	public IChannelSettings createChannel() {
		return new NetworkChannelSettings();
	}

	public static class Setup implements Function<IXNet, Void> {
		@Override
		public Void apply(IXNet api) {
			api.registerChannelType(INSTANCE);
			return null;
		}
	}

	/**
	 * Declare capabilities in a separate class, so injecting caps doesn't cause XNet classes to be loaded.
	 */
	public static final class Capabilities {
		@CapabilityInject(IWiredElement.class)
		public static Capability<IWiredElement> WIRED_ELEMENT;

		private Capabilities() {
		}
	}
}
