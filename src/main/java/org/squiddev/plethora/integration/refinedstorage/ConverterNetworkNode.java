package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects(RS.ID)
public class ConverterNetworkNode extends DynamicConverter<INetworkNodeProxy, INetworkNode> {
	@Nullable
	@Override
	public INetworkNode convert(@Nonnull INetworkNodeProxy from) {
		return from.getNode();
	}
}
