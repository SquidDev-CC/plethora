package org.squiddev.plethora.integration.mcmultipart;

import dan200.computercraft.api.lua.LuaException;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ReferenceMultipart implements IReference<IMultipart> {
	private final UUID id;
	private final IMultipartContainer container;

	public ReferenceMultipart(IMultipartContainer container, UUID id) {
		this.id = id;
		this.container = container;
	}

	public ReferenceMultipart(IMultipartContainer container, IMultipart part) {
		this(container, container.getPartID(part));
	}

	@Nonnull
	@Override
	public IMultipart get() throws LuaException {
		IMultipart part = container.getPartFromID(id);
		if (part == null) throw new LuaException("Part is no longer there");

		return part;
	}
}
