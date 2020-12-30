package org.squiddev.plethora.gameplay.modules;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.gameplay.client.RenderOverlay;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

public final class ChatMessage implements BasicMessage {
	public static final int TIME = 30;

	private int world;
	private Vec3d pos;
	private String message;

	// Client side methods
	private int count = TIME;
	private int id;

	public ChatMessage(Entity entity, ITextComponent message) {
		this(
			entity.getEntityWorld().provider.getDimension(),
			new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
			message
		);
	}

	public ChatMessage(int world, Vec3d pos, ITextComponent message) {
		setup(world, pos, message.getFormattedText());
	}

	public ChatMessage() {
	}

	private void setup(int world, Vec3d pos, String message) {
		this.world = world;
		this.pos = pos;
		this.message = message;

		id = pos.hashCode() * 31 + message.hashCode();
	}

	public boolean decrement() {
		return --count <= 0;
	}

	public int getWorld() {
		return world;
	}

	public Vec3d getPosition() {
		return pos;
	}

	public String getMessage() {
		return message;
	}

	public int getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		setup(
			buf.readInt(),
			new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
			ByteBufUtils.readUTF8String(buf)
		);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(world);
		buf.writeDouble(pos.x);
		buf.writeDouble(pos.y);
		buf.writeDouble(pos.z);
		ByteBufUtils.writeUTF8String(buf, message);
	}

	@Override
	public void onMessage(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) RenderOverlay.addMessage(this);
	}
}
