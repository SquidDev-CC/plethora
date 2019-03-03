package org.squiddev.plethora.integration.mcmultipart;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;

@Injects(MCMultiPart.MODID)
public final class IntegrationMcMultipart {
	private IntegrationMcMultipart() {
	}

	public static final DynamicConverter<IPartInfo, IBlockState> PART_INFO_TO_BLOCK_STATE = part -> {
		World world = part.getPartWorld();
		BlockPos pos = part.getPartPos();
		return part.getPart().getExtendedState(world, pos, part, part.getPart().getActualState(world, pos, part));
	};

	public static final ConstantConverter<IPartInfo, TileEntity> PART_INTO_TO_TILE = part -> {
		IMultipartTile tile = part.getTile();
		return tile == null ? null : tile.getTileEntity();
	};
}
