package org.squiddev.plethora.api.converter;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.function.Supplier;

public final class Converters {
	private Converters() {
	}

	/**
	 * Build a converter from some {@link ICapabilityProvider} to a target type using a capability.
	 *
	 * @param capSource A supplier for the capability. We need this to be lazy, as the capability may not have been
	 *                  registered when constructing this.
	 * @param <T>       The type we target.
	 * @return The dynamic converter for this capability.
	 */
	public static <T> DynamicConverter<ICapabilityProvider, T> ofCapability(Supplier<Capability<T>> capSource) {
		return from -> {
			Capability<T> cap = capSource.get();
			return cap != null && from.hasCapability(cap, null) ? from.getCapability(cap, null) : null;
		};
	}
}
