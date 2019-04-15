package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.reference.DynamicReference;

import javax.annotation.Nonnull;

public class ReferenceMultipart implements DynamicReference<TMultiPart> {
	private final TMultiPart part;
	private final TileMultipart container;

	public ReferenceMultipart(TileMultipart container, TMultiPart part) {
		this.container = container;
		this.part = part;
	}

	@Nonnull
	@Override
	public TMultiPart get() throws LuaException {
		TileMultipart tile = part.tile();
		if (tile != container) throw new LuaException("Part is no longer there");

		World world = tile.getWorld();
		BlockPos pos = tile.getPos();

		if (world == null || pos == null || !world.isBlockLoaded(pos) || world.getTileEntity(pos) != tile) {
			throw new LuaException("Part is no longer there");
		}

		return part;
	}

	@Nonnull
	@Override
	public TMultiPart safeGet() {
		return part;
	}
}
