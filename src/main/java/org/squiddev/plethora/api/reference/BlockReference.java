package org.squiddev.plethora.api.reference;

import com.google.common.base.Objects;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class BlockReference implements IReference<BlockReference> {
	private final IWorldLocation location;
	private final WeakReference<TileEntity> tile;
	private final IBlockState state;

	public BlockReference(@Nonnull IWorldLocation location, @Nonnull IBlockState state, @Nullable TileEntity tile) {
		this.location = location;
		this.tile = tile == null ? null : new WeakReference<TileEntity>(tile);
		this.state = state;
	}

	public BlockReference(@Nonnull IWorldLocation location) {
		this(location, location.getWorld().getBlockState(location.getPos()), location.getWorld().getTileEntity(location.getPos()));
	}

	@Nonnull
	@Override
	public BlockReference get() throws LuaException {
		World world = location.getWorld();
		BlockPos pos = location.getPos();

		IBlockState newState = world.getBlockState(pos);
		TileEntity newTe = world.getTileEntity(pos);

		if (!state.equals(newState)) throw new LuaException("The block is no longer there");

		if (tile == null && newTe != null) {
			throw new LuaException("The block has changed");
		} else if (tile != null) {
			TileEntity oldTe = tile.get();
			if (oldTe == null) {
				throw new LuaException("The block is no longer there");
			} else if (!oldTe.equals(newTe)) {
				throw new LuaException("The block has changed");
			}
		}

		return this;
	}

	@Nonnull
	public IWorldLocation getLocation() {
		return location;
	}

	@Nonnull
	public IBlockState getState() {
		return state;
	}

	@Nullable
	public TileEntity getTileEntity() {
		return tile == null ? null : tile.get();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BlockReference that = (BlockReference) o;

		if (!location.equals(that.location)) return false;
		if (tile != that.tile) {
			if (tile == null) return false;

			TileEntity thisTile = tile.get();
			TileEntity thatTile = that.tile == null ? null : that.tile.get();

			if (!Objects.equal(thisTile, thatTile)) return false;
		}

		if (tile != null ? !tile.equals(that.tile) : that.tile != null) return false;
		return state.equals(that.state);
	}

	@Override
	public int hashCode() {
		int result = location.hashCode();
		result = 31 * result + state.hashCode();
		return result;
	}
}
