package org.squiddev.plethora.gameplay.minecart;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageMinecartSlot implements IMessage {
	public static final byte FLAG_TAG = 1;
	public static final byte FLAG_STACK = 2;

	public int entityId;
	public int slot;
	private int flags;
	public ItemStack stack;
	public NBTTagCompound tag;

	public MessageMinecartSlot() {
	}

	public MessageMinecartSlot(EntityMinecartComputer minecart, int slot) {
		entityId = minecart.getEntityId();
		this.slot = slot;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
		flags |= FLAG_STACK;
	}

	public void setTag(NBTTagCompound tag) {
		this.tag = tag;
		flags |= FLAG_TAG;
	}

	public boolean hasTag() {
		return (flags & FLAG_TAG) != 0;
	}

	public boolean hasStack() {
		return (flags & FLAG_STACK) != 0;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		slot = buf.readByte();
		flags = buf.readByte();

		if ((flags & FLAG_TAG) != 0) tag = ByteBufUtils.readTag(buf);
		if ((flags & FLAG_STACK) != 0) stack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeByte(slot);
		buf.writeByte(flags);

		if ((flags & FLAG_TAG) != 0) ByteBufUtils.writeTag(buf, tag);
		if ((flags & FLAG_STACK) != 0) ByteBufUtils.writeItemStack(buf, stack);
	}
}
