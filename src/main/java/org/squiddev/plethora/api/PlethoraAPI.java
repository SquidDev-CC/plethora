package org.squiddev.plethora.api;

import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.IMethodRegistry;

/**
 * API entry point for Plethora
 */
public class PlethoraAPI {
	public interface IPlethoraAPI {
		IMethodRegistry methodRegistry();

		IMetaRegistry metaRegistry();
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
		final String name = "org.squiddev.plethora.impl.API";
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
