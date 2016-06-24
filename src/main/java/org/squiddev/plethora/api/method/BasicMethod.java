package org.squiddev.plethora.api.method;

/**
 * Basic wrapper for methods
 */
public abstract class BasicMethod<T> implements IMethod<T> {
	private final String name;
	private final boolean worldThread;

	protected BasicMethod(String name, boolean worldThread) {
		this.name = name;
		this.worldThread = worldThread;
	}

	public BasicMethod(String name) {
		this.name = name;
		worldThread = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean worldThread() {
		return worldThread;
	}

	@Override
	public boolean canApply(IContext<T> context) {
		return true;
	}
}
