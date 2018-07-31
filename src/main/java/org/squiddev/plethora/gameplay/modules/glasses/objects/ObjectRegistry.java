package org.squiddev.plethora.gameplay.modules.glasses.objects;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
	public static final byte GROUP_2D = 8;

	private ObjectRegistry() {
	}

	public static BaseObject create(int id, int parent, byte type) {
		switch (type) {
			case RECTANGLE_2D:
				return new Rectangle(id, parent);
			case LINE_2D:
				return new Line(id, parent);
			case DOT_2D:
				return new Dot(id, parent);
			case TEXT_2D:
				return new Text(id, parent);
			case TRIANGLE_2D:
				return new Triangle(id, parent);
			case POLYGON_2D:
				return new Polygon(id, parent);
			case LINE_LOOP_2D:
				return new LineLoop(id, parent);
			case ITEM_2D:
				return new Item2D(id, parent);
			case GROUP_2D:
				return new ObjectGroup2D(id, parent);
			default:
				throw new IllegalStateException("Unknown type " + type);
		}
	}

	public static BaseObject read(ByteBuf buf) {
		int id = ByteBufUtils.readVarInt(buf, 5);
		int parent = ByteBufUtils.readVarInt(buf, 5);
		byte type = buf.readByte();

		BaseObject object = ObjectRegistry.create(id, parent, type);
		object.readInitial(buf);
		return object;
	}

	public static void write(ByteBuf buf, BaseObject object) {
		ByteBufUtils.writeVarInt(buf, object.id(), 5);
		ByteBufUtils.writeVarInt(buf, object.parent(), 5);
		buf.writeByte(object.type());
		object.writeInitial(buf);
	}
}
