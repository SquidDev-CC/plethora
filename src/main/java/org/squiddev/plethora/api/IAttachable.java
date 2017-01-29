package org.squiddev.plethora.api;

/**
 * An object which can be "attached" and "detached" from something.
 *
 * This "something" is normally a computer. Unlike {@link dan200.computercraft.api.peripheral.IPeripheral}'s
 * methods, this is only attached to one object at a time.
 */
public interface IAttachable {
	/**
	 * Called when this object is attached to something. Will not be
	 * called twice without {@link #detach()} being called in between.
	 */
	void attach();

	/**
	 * Called when an object is detached from something. Will only be called
	 * after {@link #attach()}.
	 */
	void detach();
}
