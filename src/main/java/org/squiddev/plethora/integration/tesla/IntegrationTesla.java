package org.squiddev.plethora.integration.tesla;

import com.google.common.collect.Maps;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;

import java.util.Map;

import static org.squiddev.plethora.api.converter.Converters.ofCapability;

@Injects("tesla")
public final class IntegrationTesla {
	private IntegrationTesla() {
	}

	public static final DynamicConverter<ICapabilityProvider, ITeslaHolder> TESLA_HOLDER_CAP = ofCapability(() -> TeslaCapabilities.CAPABILITY_HOLDER);

	public static final IMetaProvider<ITeslaHolder> META_TESLA_HOLDER = new NamespacedMetaProvider<>("tesla", object -> {
		Map<Object, Object> out = Maps.newHashMap();
		ITeslaHolder handler = object.getTarget();
		out.put("stored", handler.getStoredPower());
		out.put("capacity", handler.getCapacity());
		return out;
	});
}
