package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.capability.CapabilityNetworkNodeProxy;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import static org.squiddev.plethora.api.converter.Converters.ofCapability;

@Injects(RS.ID)
public final class IntegrationRefinedStorage {
	private IntegrationRefinedStorage() {
	}

	public static final DynamicConverter<INetworkNodeProxy, INetworkNode> PROXY_TO_NODE = INetworkNodeProxy::getNode;

	public static final DynamicConverter<ICapabilityProvider, INetworkNodeProxy> NETWORK_NODE_CAP = ofCapability(
		() -> CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY
	);
}
