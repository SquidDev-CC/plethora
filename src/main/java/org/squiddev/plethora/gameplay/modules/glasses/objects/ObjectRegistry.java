package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.Rectangle;

public final class ObjectRegistry {
	public static final int RECTANGLE_2D = 0;

	private ObjectRegistry() {
	}

	public static BaseObject create(int id, byte type) {
		switch (type) {
			case RECTANGLE_2D:
				return new Rectangle(id);
			default:
				throw new IllegalStateException("Unknown type " + type);
		}
	}

	public static BaseObject read(ByteBuf buf) {
		int id = buf.readInt();
		byte type = buf.readByte();

		BaseObject object = ObjectRegistry.create(id, type);
		object.readInitial(buf);
		return object;
	}

	public static void write(ByteBuf buf, BaseObject object) {
		buf.writeInt(object.id);
		buf.writeByte(object.getType());
		object.writeInital(buf);
	}
}
