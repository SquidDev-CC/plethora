package org.squiddev.plethora.gameplay.minecart;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class MinecartHelpers {
	private static int[][][] matrix;

	private MinecartHelpers() {
	}

	private static int[][][] getMatrix() {
		if (matrix == null) {
			matrix = ObfuscationReflectionHelper.getPrivateValue(EntityMinecart.class, null, "field_70500_g");
		}

		return matrix;
	}

	/**
	 * Get the position offset of the minecart
	 *
	 * @param minecart The minecart to get the position of
	 * @param x        Interpolated x position of minecart
	 * @param y        Interpolated y position of minecart
	 * @param z        Interpolated z position of minecart
	 * @param offset   Direction to offset in
	 * @return The offfset position
	 * @see EntityMinecart#getPosOffset(double, double, double, double)
	 */
	public static Vec3d getPosOffset(EntityMinecart minecart, double x, double y, double z, double offset) {
		int xf = MathHelper.floor(x);
		int yf = MathHelper.floor(y);
		int zf = MathHelper.floor(z);

		if (BlockRailBase.isRailBlock(minecart.getEntityWorld(), new BlockPos(xf, yf - 1, zf))) --yf;

		IBlockState block = minecart.getEntityWorld().getBlockState(new BlockPos(xf, yf, zf));

		if (BlockRailBase.isRailBlock(block)) {
			BlockRailBase.EnumRailDirection railBase = ((BlockRailBase) block.getBlock()).getRailDirection(minecart.getEntityWorld(), new BlockPos(xf, yf, zf), block, minecart);
			y = yf;

			if (railBase.isAscending()) y = (double) (yf + 1);

			int[][] transformed = getMatrix()[railBase.getMetadata()];
			double dx = (double) (transformed[1][0] - transformed[0][0]);
			double dz = (double) (transformed[1][2] - transformed[0][2]);
			double len = Math.sqrt(dx * dx + dz * dz);
			dx /= len;
			dz /= len;
			x += dx * offset;
			z += dz * offset;

			if (transformed[0][1] != 0 && MathHelper.floor(x) - xf == transformed[0][0] && MathHelper.floor(z) - zf == transformed[0][2]) {
				y += (double) transformed[0][1];
			} else if (transformed[1][1] != 0 && MathHelper.floor(x) - xf == transformed[1][0] && MathHelper.floor(z) - zf == transformed[1][2]) {
				y += (double) transformed[1][1];
			}

			return minecart.getPos(x, y, z);
		} else {
			return null;
		}
	}

}
