package org.squiddev.plethora.gameplay.modules.glasses;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

import java.util.Arrays;

public class MessageCanvasAdd implements BasicMessage {
	private int canvasId;
	private BaseObject[] objects;

	public MessageCanvasAdd(int canvasId, BaseObject[] objects) {
		this.canvasId = canvasId;
		this.objects = objects;
	}

	public MessageCanvasAdd() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canvasId = buf.readInt();

		int size = buf.readInt();
		BaseObject[] objects = this.objects = new BaseObject[size];
		for (int i = 0; i < size; i++) {
			objects[i] = ObjectRegistry.read(buf);
		}

		// We sort on ID in order to guarantee parents are loaded before their children
		Arrays.sort(objects, BaseObject.SORTING_ORDER);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(canvasId);

		buf.writeInt(objects.length);
		for (BaseObject object : objects) {
			ObjectRegistry.write(buf, object);
		}
	}


	@Override
	public void onMessage(MessageContext context) {
		CanvasClient canvas = new CanvasClient(canvasId);

		for (BaseObject obj : objects) canvas.updateObject(obj);
		CanvasHandler.addClient(canvas);
	}
}
