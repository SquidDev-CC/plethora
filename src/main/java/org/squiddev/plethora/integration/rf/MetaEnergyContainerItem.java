package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.api.IEnergyContainerItem;
import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "rf", modId = "CoFHAPI|energy")
public class MetaEnergyContainerItem extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		Item item = object.getItem();
		if (item instanceof IEnergyContainerItem) {
			Map<Object, Object> out = Maps.newHashMap();
			IEnergyContainerItem handler = (IEnergyContainerItem) item;
			out.put("stored", handler.getEnergyStored(object));
			out.put("capacity", handler.getMaxEnergyStored(object));
			return out;
		} else {
			return Collections.emptyMap();
		}
	}
}
