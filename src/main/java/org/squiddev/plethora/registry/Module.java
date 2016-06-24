package org.squiddev.plethora.registry;

/**
 * Default implementation of {@link IModule}
 */
public abstract class Module implements IModule {
	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}
}
