package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.reference.BlockReference;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(BlockReference.class)
public class MetaBlockReference extends BasicMetaProvider<BlockReference> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull BlockReference reference) {
		Map<Object, Object> data = Maps.newHashMap();

		IBlockState state = reference.getState();
		Block block = state.getBlock();
		World world = reference.getLocation().getWorld();
		BlockPos pos = reference.getLocation().getPos();

		data.put("hardness", block.getBlockHardness(world, pos));

		return data;
	}
}
