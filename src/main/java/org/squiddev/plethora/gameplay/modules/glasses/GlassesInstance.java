package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;

/**
 * A proxy object for canvases, which allows using a separate getCanvas() method.
 */
public class GlassesInstance extends ConstantReference<GlassesInstance> implements IAttachable {
	private final CanvasServer canvas;

	public GlassesInstance(@Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvas = new CanvasServer(CanvasHandler.nextId(), access, player);
	}

	public CanvasServer getCanvas() {
		return canvas;
	}

	@Override
	public void attach() {
		canvas.attach();
		CanvasHandler.addServer(canvas);
	}

	@Override
	public void detach() {
		CanvasHandler.removeServer(canvas);
		canvas.detach();
	}

	@Nonnull
	@Override
	public GlassesInstance get() throws LuaException {
		return this;
	}

	@Nonnull
	@Override
	public GlassesInstance safeGet() throws LuaException {
		return this;
	}
}
