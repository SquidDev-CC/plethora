package org.squiddev.plethora.integration.vanilla.transfer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides locations for adjacent blocks and the current one
 *
 * We block secondary accesses as that allows people to do `self.north.north.north`, which is just silly
 */
@Injects
public final class TransferTileEntity implements ITransferProvider<TileEntity> {
	private final Map<String, EnumFacing> mappings;

	public TransferTileEntity() {
		Map<String, EnumFacing> mappings = this.mappings = Maps.newHashMap();
		mappings.put("below", EnumFacing.DOWN);
		mappings.put("above", EnumFacing.UP);
		for (EnumFacing facing : EnumFacing.VALUES) {
			mappings.put(facing.getName(), facing);
		}
	}

	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull TileEntity object, @Nonnull String key) {
		if (key.equals("self")) return object;

		EnumFacing facing = mappings.get(key);
		if (facing != null) {
			BlockPos newPos = object.getPos().offset(facing);
			return object.getWorld().getTileEntity(newPos);
		}

		return null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull TileEntity object) {
		HashSet<String> out = Sets.newHashSet();
		out.add("self");

		World world = object.getWorld();
		BlockPos pos = object.getPos();

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (world.getTileEntity(pos.offset(facing)) != null) out.add(facing.getName2());
		}

		return out;
	}

	@Override
	public boolean secondary() {
		return false;
	}
}
