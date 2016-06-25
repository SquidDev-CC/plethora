package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

/**
 * An reference to a tile. Ensures it is there.
 */
public class TileReference<T extends TileEntity> implements IReference<T> {
	private final WeakReference<T> tile;
	private final BlockPos pos;
	private final World world;

	public TileReference(@Nonnull T tile) {
		this.tile = new WeakReference<T>(tile);
		pos = tile.getPos();
		world = tile.getWorld();
	}

	@Nonnull
	@Override
	public T get() throws LuaException {
		T value = tile.get();
		if (value == null || world.getTileEntity(pos) != value) {
			throw new LuaException("The block is no longer there");
		}

		return value;
	}
}
