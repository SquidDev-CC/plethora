package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects
public final class MetaBlockReference extends BasicMetaProvider<BlockReference> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull BlockReference reference) {
		Map<String, Object> data = new HashMap<>();

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

	@Nonnull
	@Override
	public BlockReference getExample() {
		World world = WorldDummy.INSTANCE;
		world.setBlockState(BlockPos.ORIGIN, Blocks.DIRT.getDefaultState());
		return new BlockReference(new WorldLocation(world, BlockPos.ORIGIN));
	}
}
