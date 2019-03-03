package org.squiddev.plethora.integration.mcmultipart;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Injects(MCMultiPart.MODID)
public class ConverterPartState extends DynamicConverter<IPartInfo, IBlockState> {
	@Nullable
	@Override
	public IBlockState convert(@Nonnull IPartInfo part) {
		World world = part.getPartWorld();
		BlockPos pos = part.getPartPos();
		return part.getPart().getExtendedState(world, pos, part, part.getPart().getActualState(world, pos, part));
	}
}
