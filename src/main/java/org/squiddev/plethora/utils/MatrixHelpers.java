package org.squiddev.plethora.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public final class MatrixHelpers {
	private MatrixHelpers() {
	}

	private static final Matrix4f IDENTITY = new Matrix4f();

	private static final Matrix4f[] FACINGS;

	static {
		FACINGS = new Matrix4f[EnumFacing.VALUES.length];
		for (EnumFacing facing : EnumFacing.VALUES) {
			int x, y;
			switch (facing) {
				default:
				case DOWN:
					x = 0;
					y = 0;
					break;
				case UP:
					x = 180;
					y = 0;
					break;
				case EAST:
					x = 90;
					y = 270;
					break;
				case WEST:
					x = 90;
					y = 90;
					break;
				case NORTH:
					x = 90;
					y = 180;
					break;
				case SOUTH:
					x = 90;
					y = 0;
					break;
			}

			Matrix4f result = new Matrix4f(), temp = new Matrix4f();
			result.setIdentity();

			temp.setIdentity();
			temp.setTranslation(new Vector3f(0.5f, 0.5f, 0.5f));
			result.mul(temp);

			temp.setIdentity();
			temp.rotY(-y / 180.0f * (float) Math.PI);
			result.mul(temp);

			temp.setIdentity();
			temp.rotX(-x / 180.0f * (float) Math.PI);
			result.mul(temp);

			temp.setIdentity();
			temp.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
			result.mul(temp);

			FACINGS[facing.ordinal()] = result;
		}
	}

	public static Matrix4f matrixFor(EnumFacing facing) {
		int index = facing.ordinal();
		return index < FACINGS.length ? FACINGS[index] : IDENTITY;
	}

	public static AxisAlignedBB transform(AxisAlignedBB box, Matrix4f matrix) {
		return new AxisAlignedBB(
			(float) (matrix.m00 * box.minX + matrix.m01 * box.minY + matrix.m02 * box.minZ + matrix.m03),
			(float) (matrix.m10 * box.minX + matrix.m11 * box.minY + matrix.m12 * box.minZ + matrix.m13),
			(float) (matrix.m20 * box.minX + matrix.m21 * box.minY + matrix.m22 * box.minZ + matrix.m23),

			(float) (matrix.m00 * box.maxX + matrix.m01 * box.maxY + matrix.m02 * box.maxZ + matrix.m03),
			(float) (matrix.m10 * box.maxX + matrix.m11 * box.maxY + matrix.m12 * box.maxZ + matrix.m13),
			(float) (matrix.m20 * box.maxX + matrix.m21 * box.maxY + matrix.m22 * box.maxZ + matrix.m23)
		);
	}
}
