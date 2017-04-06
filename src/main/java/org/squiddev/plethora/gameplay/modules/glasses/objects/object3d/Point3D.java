package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import org.squiddev.plethora.api.IWorldLocation;

public class Point3D {
	public float x;
	public float y;
	public float z;

	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D() {
	}

	public void write(ByteBuf buf) {
		buf.writeFloat(x);
		buf.writeFloat(y);
		buf.writeFloat(z);
	}

	public void read(ByteBuf buf) {
		x = buf.readFloat();
		y = buf.readFloat();
		z = buf.readFloat();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point3D point3D = (Point3D) o;

		return Float.compare(point3D.x, x) == 0 && Float.compare(point3D.y, y) == 0 && Float.compare(point3D.z, z) == 0;
	}

	@Override
	public int hashCode() {
		int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
		return result;
	}

	public void offset(IWorldLocation location) {
		BlockPos vec = location.getPos();
		x += vec.getX();
		y += vec.getY();
		z += vec.getZ();
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + "}";
	}
}
