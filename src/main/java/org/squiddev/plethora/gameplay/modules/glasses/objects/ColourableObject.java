package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;

public abstract class ColourableObject extends BaseObject implements Colourable {
	private int colour = DEFAULT_COLOUR;

	public ColourableObject(int id) {
		super(id);
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

	protected final void setupColour() {
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
	}

	@Override
	public void writeInital(ByteBuf buf) {
		buf.writeInt(colour);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		colour = buf.readInt();
	}
}
