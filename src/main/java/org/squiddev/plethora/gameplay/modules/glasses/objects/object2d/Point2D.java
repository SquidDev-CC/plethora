package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;

public class Point2D {
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
}
