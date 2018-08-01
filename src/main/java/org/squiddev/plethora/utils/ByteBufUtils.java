package org.squiddev.plethora.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;

public class ByteBufUtils extends net.minecraftforge.fml.common.network.ByteBufUtils {
	public static void writeVec3d(ByteBuf buf, Vec3d vec) {
		buf.writeFloat((float) vec.x);
		buf.writeFloat((float) vec.y);
		buf.writeFloat((float) vec.z);
	}

	public static Vec3d readVec3d(ByteBuf buf) {
		return new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
	}

	public static void writeVec2d(ByteBuf buf, Vec2d vec) {
		buf.writeFloat((float) vec.x);
		buf.writeFloat((float) vec.y);
	}

	public static Vec2d readVec2d(ByteBuf buf) {
		return new Vec2d(buf.readFloat(), buf.readFloat());
	}
}
