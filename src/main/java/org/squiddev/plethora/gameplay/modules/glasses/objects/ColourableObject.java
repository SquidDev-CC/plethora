package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

	@SideOnly(Side.CLIENT)
	protected final void setupColour() {
		GlStateManager.color(((colour >> 24) & 0xFF) / 255.0f, ((colour >> 16) & 0xFF) / 255.0f, ((colour >> 8) & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f);
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
