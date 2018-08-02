package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;

public abstract class ColourableObject extends BaseObject implements Colourable {
	private int colour = DEFAULT_COLOUR;

	public ColourableObject(int id, int parent, byte type) {
		super(id, parent, type);
	}

	@Override
	public final int getColour() {
		return colour;
	}

	@Override
	public final void setColour(int colour) {
		if (this.colour != colour) {
			this.colour = colour;
			setDirty();
		}
	}

	protected int getRed() {
		return (colour >> 24) & 0xFF;
	}

	protected int getGreen() {
		return (colour >> 16) & 0xFF;
	}

	protected int getBlue() {
		return (colour >> 8) & 0xFF;
	}

	protected int getAlpha() {
		return (colour) & 0xFF;
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		buf.writeInt(colour);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
	}
}
