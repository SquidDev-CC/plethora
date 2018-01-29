package org.squiddev.plethora.integration.mcmultipart;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipart2;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IMultipart.class, modId = MCMultiPartMod.MODID)
public class ConverterPartState extends DynamicConverter<IMultipart, IBlockState> {
	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public IBlockState convert(@Nonnull IMultipart part) {
		World world = part.getWorld();
		BlockPos pos = part.getPos();

		IBlockState base = MultipartRegistry.getDefaultState(part).getBaseState();
		if (!(part instanceof IMultipart2) || world == null || pos == null) {
			return part.getExtendedState(part.getActualState(base));
		} else {
			IMultipart2 part2 = (IMultipart2) part;

			return part2.getExtendedState(part2.getActualState(base, world, pos), world, pos);
		}
	}
}
