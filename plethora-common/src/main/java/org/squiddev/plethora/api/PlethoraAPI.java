package org.squiddev.plethora.api;

import org.squiddev.plethora.api.converter.IConverterRegistry;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.IMethodRegistry;
import org.squiddev.plethora.api.module.IModuleRegistry;
import org.squiddev.plethora.api.transfer.ITransferRegistry;

/**
 * API entry point for Plethora
 */
public final class PlethoraAPI {
	private PlethoraAPI() {
	}

	public interface IPlethoraAPI {
		IMethodRegistry methodRegistry();

		IMetaRegistry metaRegistry();

		IConverterRegistry converterRegistry();

		ITransferRegistry transferRegistry();

		IModuleRegistry moduleRegistry();
	}

	private static final IPlethoraAPI API;

	/**
	 * Get the main API entry point
	 *
	 * @return Main API entry point
	 */
	public static IPlethoraAPI instance() {
		return API;
	}

	static {
		IPlethoraAPI api;
		final String name = "org.squiddev.plethora.core.API";
		try {
			Class<?> registryClass = Class.forName(name);
			api = (IPlethoraAPI) registryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new CoreNotFoundException("Cannot load Plethora API as " + name + " cannot be found", e);
		} catch (InstantiationException e) {
			throw new CoreNotFoundException("Cannot load Plethora API as " + name + " cannot be created", e);
		} catch (IllegalAccessException e) {
			throw new CoreNotFoundException("Cannot load Plethora API as " + name + " cannot be accessed", e);
		}
		API = api;
	}

}
