package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "energy")
public class MetaEnergyProvider implements IMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<IEnergyStorage> object) {
		Map<Object, Object> out = Maps.newHashMap();
		IEnergyStorage handler = object.getTarget();
		out.put("stored", handler.getEnergyStored());
		out.put("capacity", handler.getMaxEnergyStored());
		return out;
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
