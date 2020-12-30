package org.squiddev.plethora.integration.mcmultipart;

import dan200.computercraft.api.lua.LuaException;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import org.squiddev.plethora.api.reference.DynamicReference;

import javax.annotation.Nonnull;

public class ReferenceMultipart implements DynamicReference<IPartInfo> {
	private final IPartSlot slot;
	private final IMultipartContainer container;

	public ReferenceMultipart(IMultipartContainer container, IPartSlot slot) {
		this.slot = slot;
		this.container = container;
	}

	public ReferenceMultipart(IMultipartContainer container, IPartInfo slot) {
		this(container, slot.getSlot());
	}

	@Nonnull
	@Override
	public IPartInfo get() throws LuaException {
		IPartInfo part = container.get(slot).orElse(null);
		if (part == null) throw new LuaException("Part is no longer there");

		return part;
	}

	@Nonnull
	@Override
	public IPartInfo safeGet() throws LuaException {
		return get();
	}
}
