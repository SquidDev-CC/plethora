package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@IMetaProvider.Inject(value = IEnergyStorage.class, namespace = "energy")
public class MetaEnergyProvider extends BasicMetaProvider<IEnergyStorage> {
	public MetaEnergyProvider() {
		super("Provides the currently stored energy and capacity of a Forge Energy cell");
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IEnergyStorage handler) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("stored", handler.getEnergyStored());
		out.put("capacity", handler.getMaxEnergyStored());
		return out;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Nullable
	@Override
	public IEnergyStorage getExample() {
		return new EnergyStorage(50000, 100, 100, 1000);
	}
}
