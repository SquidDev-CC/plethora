package org.squiddev.plethora.integration.tesla;

import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.implementation.BaseTeslaContainer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.converter.Converters.ofCapability;

@Injects("tesla")
public final class IntegrationTesla {
	private IntegrationTesla() {
	}

	public static final DynamicConverter<ICapabilityProvider, ITeslaHolder> TESLA_HOLDER_CAP = ofCapability(() -> TeslaCapabilities.CAPABILITY_HOLDER);

	public static final SimpleMetaProvider<ITeslaHolder> META_TESLA_HOLDER = new BasicMetaProvider<ITeslaHolder>() {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ITeslaHolder handler) {
			Map<String, Object> out = new HashMap<>();
			out.put("stored", handler.getStoredPower());
			out.put("capacity", handler.getCapacity());
			return Collections.singletonMap("tesla", out);
		}

		@Nonnull
		@Override
		public ITeslaHolder getExample() {
			return new BaseTeslaContainer(100, 50000, 0, 0);
		}
	};
}
