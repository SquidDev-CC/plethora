package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;

public final class ObjectRegistry {
	public static final byte RECTANGLE_2D = 0;
	public static final byte LINE_2D = 1;
	public static final byte DOT_2D = 2;
	public static final byte TEXT_2D = 3;
	public static final byte TRIANGLE_2D = 4;
	public static final byte POLYGON_2D = 5;
	public static final byte LINE_LOOP_2D = 6;
	public static final byte ITEM_2D = 7;

	private ObjectRegistry() {
	}

	public static BaseObject create(int id, byte type) {
		switch (type) {
			case RECTANGLE_2D:
				return new Rectangle(id);
			case LINE_2D:
				return new Line(id);
			case DOT_2D:
				return new Dot(id);
			case TEXT_2D:
				return new Text(id);
			case TRIANGLE_2D:
				return new Triangle(id);
			case POLYGON_2D:
				return new Polygon(id);
			case LINE_LOOP_2D:
				return new LineLoop(id);
			case ITEM_2D:
				return new Item2D(id);
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
