package org.squiddev.plethora.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ByteBufUtils extends net.minecraftforge.fml.common.network.ByteBufUtils {
	public static void writeVec3d(ByteBuf buf, @Nonnull Vec3d vec) {
		buf.writeFloat((float) vec.x);
		buf.writeFloat((float) vec.y);
		buf.writeFloat((float) vec.z);
	}

	@Nonnull
	public static Vec3d readVec3d(ByteBuf buf) {
		return new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
	}

	public static void writeOptVec3d(ByteBuf buf, @Nullable Vec3d vec) {
		if (vec == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			writeVec3d(buf, vec);
		}
	}

	@Nullable
	public static Vec3d readOptVec3d(ByteBuf buf) {
		return buf.readBoolean() ? readVec3d(buf) : null;
	}

	public static void writeVec2d(ByteBuf buf, Vec2d vec) {
		buf.writeFloat((float) vec.x);
		buf.writeFloat((float) vec.y);
	}

	public static Vec2d readVec2d(ByteBuf buf) {
		return new Vec2d(buf.readFloat(), buf.readFloat());
	}
}
