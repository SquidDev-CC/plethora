package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.raoulvdberge.refinedstorage.capability.CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY;

@Injects(RS.ID)
public class ConverterNetworkNodeCapability extends DynamicConverter<ICapabilityProvider, INetworkNode> {
	@Nullable
	@Override
	public INetworkNode convert(@Nonnull ICapabilityProvider from) {
		if (!from.hasCapability(NETWORK_NODE_PROXY_CAPABILITY, null)) return null;

		INetworkNodeProxy<?> proxy = from.getCapability(NETWORK_NODE_PROXY_CAPABILITY, null);
		return proxy == null ? null : proxy.getNode();
	}
}
