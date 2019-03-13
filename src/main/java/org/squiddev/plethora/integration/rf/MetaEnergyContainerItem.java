package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(RedstoneFluxProps.MOD_ID)
public final class MetaEnergyContainerItem extends ItemStackMetaProvider<IEnergyContainerItem> {
	public MetaEnergyContainerItem() {
		super("rf", IEnergyContainerItem.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object, @Nonnull IEnergyContainerItem handler) {
		Map<Object, Object> out = new HashMap<>(2);
		out.put("stored", handler.getEnergyStored(object));
		out.put("capacity", handler.getMaxEnergyStored(object));
		return out;
	}
}
