package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Injects
public final class MetaBlockReference extends BasicMetaProvider<BlockReference> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull BlockReference reference) {
		Map<Object, Object> data = Maps.newHashMap();

		IBlockState state = reference.getState();
		World world = reference.getLocation().getWorld();
		BlockPos pos = reference.getLocation().getPos();

		data.put("hardness", state.getBlockHardness(world, pos));

		MapColor mapCol = state.getMapColor(world, pos);
		if (mapCol != null) {
			int colour = mapCol.colorValue;
			data.put("colour", colour);
			data.put("color", colour);
		}

		return data;
	}

	@Nullable
	@Override
	public BlockReference getExample() {
		World world = WorldDummy.INSTANCE;
		world.setBlockState(BlockPos.ORIGIN, Blocks.DIRT.getDefaultState());
		return new BlockReference(new WorldLocation(world, BlockPos.ORIGIN));
	}
}
