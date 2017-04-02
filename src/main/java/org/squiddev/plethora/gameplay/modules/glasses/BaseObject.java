package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

public abstract class BaseObject {
	public final int id;

	private boolean dirty = false;

	public BaseObject(int id) {
		this.id = id;
	}

	/**
	 * Get the type of this object
	 *
	 * @return The object's type
	 */
	public abstract byte getType();

	public boolean isDirty() {
		return dirty;
	}

	void resetDirty() {
		dirty = false;
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
	 * Draw this object in the 3D context.
	 */
	@SideOnly(Side.CLIENT)
	public abstract void draw3D(Tessellator tessellator);

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

	private static class BaseObjectReference implements IReference<BaseObject> {
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
}
