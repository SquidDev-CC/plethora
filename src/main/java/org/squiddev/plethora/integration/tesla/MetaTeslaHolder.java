package org.squiddev.plethora.integration.tesla;

import com.google.common.collect.Maps;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.lib.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "energy", modId = Constants.MOD_ID)
public class MetaTeslaHolder implements IMetaProvider<ITeslaHolder> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ITeslaHolder> object) {
		Map<Object, Object> out = Maps.newHashMap();
		ITeslaHolder handler = object.getTarget();
		out.put("stored", handler.getStoredPower());
		out.put("capacity", handler.getCapacity());
		return out;
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
