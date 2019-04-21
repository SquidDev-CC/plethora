package org.squiddev.plethora.integration.ic2;

import ic2.api.item.IElectricItemManager;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(IC2.MODID)
public final class MetaEnergyItem extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack object) {
		IElectricItemManager manager = IntegrationIc2.getManager(object);
		if (manager == null) return Collections.emptyMap();

		Map<String, Object> map = new HashMap<>(3);
		map.put("stored", manager.getCharge(object));
		map.put("capacity", manager.getMaxCharge(object));
		map.put("tier", manager.getTier(object));

		return Collections.singletonMap("eu", map);
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return ItemName.batpack.getItemStack();
	}
}
