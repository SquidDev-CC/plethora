package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * A proxy object for canvases, which allows using a separate getCanvas() method.
 */
public class GlassesInstance implements IAttachable, IReference<GlassesInstance> {
	private final CanvasServer canvas;

	public GlassesInstance(@Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvas = new CanvasServer(CanvasHandler.nextId(), access, player);
	}

	public CanvasServer getCanvas() {
		return canvas;
	}

	@Override
	public void attach() {
		CanvasHandler.addServer(canvas);
	}

	@Override
	public void detach() {
		CanvasHandler.removeServer(canvas);
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
