package org.squiddev.plethora.integration.vanilla.transfer;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides locations for adjacent blocks and the current one
 */
@ITransferProvider.Inject(TileEntity.class)
public class TransferTileEntity implements ITransferProvider<TileEntity> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull TileEntity object, @Nonnull String key) {
		if (key.equals("self")) return object;

		EnumFacing facing = EnumFacing.byName(key);
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
}
