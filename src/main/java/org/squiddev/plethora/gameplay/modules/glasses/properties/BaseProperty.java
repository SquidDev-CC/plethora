package org.squiddev.plethora.gameplay.modules.glasses.properties;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;

public abstract class BaseProperty<T> {
	private T value;
	private boolean dirty;

	public final int index;

	public BaseProperty(int index) {
		this.index = index;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		if (!Objects.equal(value, this.value)) {
			this.value = value;
			dirty = true;
		}
	}

	/**
	 * Write the initial buffer for this object.
	 *
	 * @param buf The buffer to write to.
	 */
	public abstract void writeInital(ByteBuf buf);

	/**
	 * Read the initial data for this object.
	 *
	 * @param buf The buffer to read from.
	 */
	public abstract void readInitial(ByteBuf buf);

	/**
	 * Write the modified data for this object.
	 *
	 * @param buf The buffer to write to.
	 */
	public abstract void writeUpdate(ByteBuf buf);

	/**
	 * Read the modified data for this object.
	 *
	 * @param buf The buffer to read from.
	 */
	public abstract void readUpdate(ByteBuf buf);
}
