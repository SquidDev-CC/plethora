package org.squiddev.plethora.utils;

public class Vec2d {
	public static final Vec2d ZERO = new Vec2d(0.0d, 0.0d);

	/**
	 * X coordinate of Vec2D
	 */
	public final double x;

	/**
	 * Y coordinate of Vec2D
	 */
	public final double y;

	public Vec2d(double x, double y) {
		if (x == -0.0D) x = 0.0D;
		if (y == -0.0D) y = 0.0D;

		this.x = x;
		this.y = y;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Vec2d)) {
			return false;
		} else {
			Vec2d other = (Vec2d) object;

			return Double.compare(other.x, this.x) == 0 && Double.compare(other.y, this.y) == 0;
		}
	}

	public int hashCode() {
		long j = Double.doubleToLongBits(this.x);
		int i = (int) (j ^ j >>> 32);
		j = Double.doubleToLongBits(this.y);
		i = 31 * i + (int) (j ^ j >>> 32);
		return i;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
}
