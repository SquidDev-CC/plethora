package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;

public final class Point2D {
	public float x;
	public float y;

	public Point2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point2D() {
	}

	public void write(ByteBuf buf) {
		buf.writeFloat(x);
		buf.writeFloat(y);
	}

	public void read(ByteBuf buf) {
		x = buf.readFloat();
		y = buf.readFloat();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point2D point2D = (Point2D) o;
		return Float.compare(point2D.x, x) == 0 && Float.compare(point2D.y, y) == 0;
	}

	@Override
	public int hashCode() {
		int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		return result;
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + "}";
	}
}
