package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

public abstract class BaseObject {
	private final int id;
	private final byte type;

	private boolean dirty = false;

	public BaseObject(int id, byte type) {
		this.id = id;
		this.type = type;
	}

	/**
	 * Get the unique ID for this object
	 *
	 * @return This object's ID
	 */
	public final int id() {
		return id;
	}

	/**
	 * Get the type of this object
	 *
	 * @return The object's type
	 */
	public final byte type() {
		return type;
	}

	boolean pollDirty() {
		boolean value = dirty;
		dirty = false;
		return value;
	}

	protected void setDirty() {
		dirty = true;
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
	public void writeUpdate(ByteBuf buf) {
		writeInital(buf);
	}

	/**
	 * Read the modified data for this object.
	 *
	 * @param buf The buffer to read from.
	 */
	public void readUpdate(ByteBuf buf) {
		readInitial(buf);
	}

	/**
	 * Draw this object in the 2D context.
	 */
	@SideOnly(Side.CLIENT)
	public abstract void draw2D();

	/**
	 * Get a reference to this object
	 *
	 * @param canvas The owning canvas
	 * @return The resulting reference.
	 */
	public IReference<BaseObject> reference(CanvasServer canvas) {
		return new BaseObjectReference(canvas, this);
	}

	private static class BaseObjectReference extends ConstantReference<BaseObject> {
		private final CanvasServer canvas;
		private final int id;

		public BaseObjectReference(CanvasServer canvas, BaseObject object) {
			this.canvas = canvas;
			this.id = object.id;
		}


		@Nonnull
		@Override
		public BaseObject get() throws LuaException {
			BaseObject object = canvas.getObject(id);
			if (object == null) throw new LuaException("This object has been removed");
			return object;
		}

		@Nonnull
		@Override
		public BaseObject safeGet() throws LuaException {
			return get();
		}
	}

	/**
	 * Prepare to draw a flat object.
	 */
	@SideOnly(Side.CLIENT)
	protected static void setupFlat() {
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
	}
}
