package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.gameplay.modules.GlassesHandler;

import javax.annotation.Nonnull;

public class GlassesInstance implements IAttachable, IReference<GlassesInstance> {
	private final IModuleAccess access;
	private final EntityPlayerMP player;

	public GlassesInstance(@Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.access = access;
		this.player = player;
	}

	@Nonnull
	public EntityPlayerMP getPlayer() {
		return player;
	}

	@Override
	public void attach() {
		GlassesHandler.add(this);
	}

	@Override
	public void detach() {
		GlassesHandler.remove(this);
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
