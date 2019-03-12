package org.squiddev.plethora.integration.ic2;

import com.google.common.collect.Maps;
import ic2.api.item.IElectricItemManager;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = IC2.MODID, namespace = "eu")
public class MetaEnergyItem extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		IElectricItemManager manager = IntegrationIc2.getManager(object);
		if (manager == null) return Collections.emptyMap();

		Map<Object, Object> map = Maps.newHashMap();
		map.put("stored", manager.getCharge(object));
		map.put("capacity", manager.getMaxCharge(object));
		map.put("tier", manager.getTier(object));

		return map;
	}
}
