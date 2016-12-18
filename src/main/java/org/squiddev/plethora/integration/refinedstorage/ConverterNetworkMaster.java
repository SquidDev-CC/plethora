package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(modId = RS.ID, value = INetworkNode.class)
public class ConverterNetworkMaster implements IConverter<INetworkNode, INetworkMaster> {
	@Nullable
	@Override
	public INetworkMaster convert(@Nonnull INetworkNode from) {
		return from.getNetwork();
	}
}
